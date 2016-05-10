(defproject build-monitor "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [devcards "0.2.1-6"]
                 [reagent "0.5.1"]
                 [org.clojure/core.async "0.2.374"]
                 [cljs-http "0.1.38"]
                 [prismatic/schema "1.0.4"]
                 [cljsjs/js-yaml "3.3.1-0"]
                 [cljsjs/bootstrap "3.3.6-0"]
                 [cljsjs/spin "2.3.2-0"]
                 [com.andrewmcveigh/cljs-time "0.3.14"]
                 [cljsjs/moment "2.10.6-0"]
                 [cljsjs/react-select "1.0.0-beta10-1"]
                 [funcool/hodgepodge "0.1.4"]
                 [org.clojars.frozenlock/reagent-modals "0.2.3"]
                 [cljsjs/react-reorderable "0.2.2-0"]]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-6"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"]

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[figwheel-sidecar "0.5.0-6"]
                                  [com.cemerick/piggieback "0.2.1"]]}}
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :source-paths ["src" "dev" "test"]

  :figwheel { :css-dirs ["resources/public/css"] })
