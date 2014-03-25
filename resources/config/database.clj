{:development {:db "stockroom_development"
               :user "root"
               :make-pool? true
               :test-connection-on-checkout true
               :useLegacyDatetimeCode false
               :serverTimezone "UTC"}
 :test {:db "stockroom_test"
        :useLegacyDatetimeCode false
        :serverTimezone "UTC"
        :user "root"
        :make-pool? true}
 }
