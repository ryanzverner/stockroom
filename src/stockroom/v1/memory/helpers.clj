(ns stockroom.v1.memory.helpers
  (:require [chee.datetime :as cd]))

(defn present? [data key]
  (key data))

(defn find-by-id [db type id]
  (get-in db [type id]))

(defn with-create-timestamps [data]
  (let [now (cd/now)]
    (assoc data
           :created-at now
           :updated-at now)))

(defn with-update-timestamps [data]
  (let [now (cd/now)]
    (-> data
        (assoc :updated-at now)
        (dissoc :created-at))))

(defn with-update-data [data]
  (-> data
      (dissoc :id)
      (with-update-timestamps)))

(defn insert [db type data]
  (let [new-id (str (inc (count (type db))))
        data (with-create-timestamps data)
        data (assoc data :id new-id)]
    [(assoc-in db [type new-id] data)
     new-id]))

(defn all-data-for-type
  ([db type] (all-data-for-type db type {}))
  ([db type options]
   (let [post-fns []
         post-fns (if-let [ns (:namespace options)]
                    (conj post-fns #(map (fn [row]
                                           (reduce
                                             (fn [acc [k v]]
                                               (assoc acc (keyword ns (name k)) v))
                                             {} row)) %))
                    post-fns)
         post (apply comp post-fns)]
     (-> db type vals post))))

(defn join [left right f]
  (reduce
    (fn [results left-row]
      (reduce
        (fn [results right-row]
          (if (f left-row right-row)
            (conj results (merge left-row right-row))
            results))
        results
        right))
    []
    left))

(defn find-where [db type f]
  (let [all-data (get db type)]
    (for [[id data] all-data :when (f data)] data)))

(defn update-where [db type finder updater]
  (->> (get db type)
       (reduce
         (fn [acc [id data]]
           (if (finder data)
             (assoc acc id (->> data updater with-update-data (merge data)))
             (assoc acc id data)))
         {})
       (assoc db type)))

(defn remove-where [db type f]
  (let [all-data (get db type)]
    (->> (for [[id data] all-data :when (f data)] id)
         (apply dissoc all-data)
         (assoc db type))))

(defn find-by-name [db type name]
  (->> (vals (db type))
       (filter #(= (% :name) name))
       first))

(defn keep-keys-with-ns [m ns-name]
  (reduce
    (fn [m [k v]]
      (if (= (namespace k) ns-name)
        m
        (dissoc m k)))
    m m))

(defn strip-nses-from-keys [m]
  (reduce
    (fn [m [k v]]
      (assoc m (keyword (name k)) v))
    {} m))

(defn select-keys-with-ns [m ns-name]
  (-> m
      (keep-keys-with-ns ns-name)
      (strip-nses-from-keys)))

(defn find-group-by-name [db name]
  (->> (all-data-for-type db :groups)
       (filter #(= (:name %) name))
       (first)))

(defn compare-fn [accessor direction]
  (let [multiplier (if (= :desc direction) -1 1)]
    (fn [a b]
      (* multiplier
         (let [av (accessor a)
               bv (accessor b)]
           (cond
             (and (nil? av) (nil? bv)) 0
             (nil? av) 1
             (nil? bv) -1
             :else (.compareTo av bv)))))))

(deftype FnComparator [compare-fns]
  java.util.Comparator
  (compare [this a b]
    (or
      (some
        #(if (zero? %) false %)
        (map (fn [compare-fn] (compare-fn a b)) compare-fns))
      0)))

(defn comparator-for-fns [compare-fns]
  (FnComparator. compare-fns))

(defn compare-values [compare-fn a b]
  (compare-fn (compare a b) 0))

(defn date-range-intersection-filter [start end]
  (let [greater-than-or-equal-to-start? (fn [date] (>= (compare date start) 0))
        less-than-or-equal-to-start? (fn [date] (<= (compare date start) 0))
        greater-than-or-equal-to-end? (fn [date] (>= (compare date end) 0))
        less-than-or-equal-to-end? (fn [date] (<= (compare date end) 0))
        in-range? #(and (greater-than-or-equal-to-start? %)
                        (less-than-or-equal-to-end? %))
        both-outside-of-range? #(and (less-than-or-equal-to-start? %1)
                                     (greater-than-or-equal-to-end? %2))]
    (if (and start end)
      #(or (in-range? (:start %))
           (in-range? (:end %))
           (both-outside-of-range? (:start %) (:end %)))
      (fn [_] (constantly true)))))
