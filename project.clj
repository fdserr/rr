(defproject rr "0.1.1"
  :description "Redux redux for ClojureScript."
  :url "https://github.com/fdserr/rr"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/clojurescript "1.9.293"]
                 [rum "0.10.7"]]

  :plugins [[lein-figwheel "0.5.8"]
            [lein-cljsbuild "1.1.4" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src" "test"]
                :figwheel {:on-jsload "rr.core/on-js-reload"}
                :compiler {:main rr.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/rr.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}
                          ;  :preloads [devtools.preload]}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/rr.js"
                           :main rr.core
                           :optimizations :whitespace
                           :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]}
            ;; :http-server-root "public"}
            ;; default and assumes "resources"
            ;; :server-port 3449 ;; default
            ;; :server-ip "127.0.0.1"
            ;; Start an nREPL server into the running figwheel process
            ;; :nrepl-port 7888
            ;; Server Ring Handler (optional)
            ;; :ring-handler hello_world.server/handler
            ;; if you want to disable the REPL
            ;; :repl false
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]
                                  [binaryage/devtools "0.8.2"]
                                  [figwheel-sidecar "0.5.8"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src" "dev" "test"]
                   ;; for CIDER
                   ;; :plugins [[cider/cider-nrepl "0.12.0"]]
                   :repl-options {; for nREPL dev you really need to limit output
                                  :init (set! *print-length* 50)
                                  :nrepl-middleware
                                   [cemerick.piggieback/wrap-cljs-repl]}}})
