(ns stockroom.admin.apprenticeships.new-apprenticeship-spec
  (:require [speclj.core :refer :all]
            [stockroom.admin.spec-helper :refer :all]
            [stockroom.admin.apprenticeships.new-apprenticeship :refer :all]
            [stockroom.admin.url-helper :as urls]))

(describe "stockroom.admin.apprenticeships.new-apprenticeship"
  
  (with ctx (test-admin-context))

  (it "builds view data for the new apprenticeship view"
    (let [errors {:a ["one" "two"]}
          params {:a 1 :b 2}
          people [{:id 11 :first-name "Sally" :last-name "Jones"}
                  {:id 10 :first-name "John" :last-name "Smith"}]]
      (should= {:create-apprenticeship-url (urls/create-apprenticeship-url @ctx)
                :errors errors
                :params params
                :person-options [["Select a Person" ""]
                                 ["John Smith" "10"]
                                 ["Sally Jones" "11"]]}
               (build-view-data-for-new-apprenticeship-view {:context @ctx
                                                             :params params
                                                             :errors errors
                                                             :people people})))))
