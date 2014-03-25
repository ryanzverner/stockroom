(let [common {:prefix "/assets"
              :output-dir "resources/public"}
      development (assoc common
                         :load-paths [{:type :directory
                                       :path "src/assets/scss"}
                                      {:type :directory
                                       :path "src/assets/fonts"}
                                      {:type :directory
                                       :path "src/assets/javascripts"}]
                         :use-digest-path false
                         :plugins [:sass :compass]
                         :strategy :runtime)]
  {:development development
   :precompile (assoc development
                      :use-digest-path true
                      :compress true)
   :production (assoc common
                      :strategy :precompiled
                      :pipeline-enabled false
                      :use-digest-path true)})
