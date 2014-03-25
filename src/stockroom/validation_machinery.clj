(ns stockroom.validation-machinery)

(defn- bind [mv validation]
  (fn [errors]
    (let [[args new-errors] (mv errors)]
      ((validation args) new-errors))))

(defn- validation-chain [args & validations]
  (fn [errors]
    (let [init_mv (fn [init-errors] [args init-errors])]
      ((reduce bind init_mv validations) errors))))

(defn- branch [branch-pred preds validations]
  (fn [args]
    (loop [preds preds]
      (if (seq preds)
        (if (branch-pred (apply (first preds) args))
          (recur (rest preds))
          (fn [errors]
            [args errors]))
        (apply validation-chain args validations)))))

(defn validate-> [args & validations]
  ((apply validation-chain args validations) []))

(defn validate [pred message]
  (fn [args]
    (fn [errors]
      (if (apply pred args)
        [args errors]
        [args (conj errors message)]))))

(defn return-errors [args] identity)

(defn unless [preds & validations]
  (branch not preds validations))

(defn only-if [preds & validations]
  (branch identity preds validations))

(defn alter-arguments-via [f]
  (fn [args]
    (fn [errors]
      [(apply f args) errors])))

(defmacro defvalidator [name args & body]
  `(defn ~name [~@args]
     (validate-> [~@args] ~@body)))
