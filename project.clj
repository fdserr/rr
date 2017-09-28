(defproject rr "1.0.0-SNAPSHOT"
  :description "Redux redux."
  :url "https://github.com/fdserr/rr"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript "1.9.854"]
                 [devcards "0.2.3"]
                 [sablono "0.7.4"]]


  :plugins [[lein-figwheel "0.5.13"]
            [lein-cljsbuild "1.1.7" :exclusions [org.clojure/clojure]]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"]

  :source-paths ["src"]

  :cljsbuild {
              :builds [{:id "devcards"
                        :source-paths ["src"]
                        :figwheel {:devcards true}
                        :compiler {:main       "rr.index"
                                   :asset-path "js/compiled/devcards_out"
                                   :output-to  "resources/public/js/compiled/rr_devcards.js"
                                   :output-dir "resources/public/js/compiled/devcards_out"
                                   :source-map-timestamp true}}
                       {:id "dev"
                        :source-paths ["src"]
                        :figwheel true
                        :compiler {:main       "rr.core"
                                   :asset-path "js/compiled/out"
                                   :output-to  "resources/public/js/compiled/rr.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :source-map-timestamp true}}
                       {:id "prod"
                        :source-paths ["src"]
                        :compiler {:main       "rr.core"
                                   :asset-path "js/compiled/out"
                                   :output-to  "resources/public/js/compiled/rr.js"
                                   :optimizations :advanced}}]}

  :figwheel { :css-dirs ["resources/public/css"]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.2"]
                                  [figwheel-sidecar "0.5.13"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src" "dev"]
                   ;; for CIDER
                   ;; :plugins [[cider/cider-nrepl "0.12.0"]]
                   :repl-options {; for nREPL dev you really need to limit output
                                  :init (set! *print-length* 50)
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
