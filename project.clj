(defproject pik-logistic-dashboard "0.1.0-SNAPSHOT"


  :min-lein-version "2.8.1"


  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.3.465"]
                 [com.cognitect/transit-cljs "0.8.243"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.2"]
                 [day8.re-frame/http-fx "0.1.4"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [cljsjs/moment "2.17.1-1"]]


  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-figwheel "0.5.14"]
            [lein-cooper "1.2.2"]]


  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.8"]
                                  [figwheel-sidecar "0.5.14"]
                                  [com.cemerick/piggieback "0.2.2"]
                                  [re-frisk "0.5.3"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :cljsbuild
                                 {:builds {:client {:source-paths ["src/cljs" "dev"]
                                                    :compiler {:asset-path "js/compiled/out"
                                                               :output-dir "resources/public/js/compiled/out"
                                                               :optimizations :none
                                                               :source-map true
                                                               :source-map-timestamp true
                                                               :preloads [devtools.preload re-frisk.preload]
                                                               :external-config {:devtools/config {:features-to-install :all}}}
                                                    :figwheel {:on-jsload "pik-logistic-dashboard.core/mount-root"}}}}}


             :prod {:cljsbuild
                    {:builds {:client {:source-paths ["src/cljs"]
                                       :compiler {:optimizations :advanced
                                                  :closure-defines {goog.DEBUG false}
                                                  :pretty-print false}}}}}}


  :figwheel {:css-dirs ["resources/public/css"]
             :nrepl-port 7888}


  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds {:client {:compiler {:main pik-logistic-dashboard.core
                                           :output-to "resources/public/js/compiled/app-v0.2.6.js"}}}})
