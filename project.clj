(defproject pik-logistic-dashboard "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0-RC1"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/clojurescript "1.9.946"]
                 [com.cognitect/transit-cljs "0.8.243"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.2"]
                 [day8.re-frame/http-fx "0.1.4"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [cljsjs/moment "2.17.1-1"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-cooper "1.2.2"]]

  :min-lein-version "2.8.1"

  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]
             :nrepl-port 7888}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.7"]
                   [figwheel-sidecar "0.5.14"]
                   [com.cemerick/piggieback "0.2.2"]
                   [re-frisk "0.5.2"]]
    :source-paths ["src/cljs" "dev"]
    :plugins      [[lein-figwheel "0.5.14"]]
    :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "pik-logistic-dashboard.core/mount-root"}
                    ;:websocket-host "192.168.225.241"}
     :compiler     {:main                 pik-logistic-dashboard.core
                    :output-to            "resources/public/js/compiled/app-v0.2.3.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload re-frisk.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}}}


    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            pik-logistic-dashboard.core
                    :output-to       "resources/public/js/compiled/app-v0.2.3.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}]})





