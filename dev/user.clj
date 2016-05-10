(ns user
  (:use [figwheel-sidecar.repl-api :as ra]))

(def figwheel-config
  {:figwheel-options {:css-dirs ["resources/public/css"]
                      :open-file-command "emacsclient.bat"} ;; <-- figwheel server config goes here
   :build-ids ["devcards"]   ;; <-- a vector of build ids to start autobuilding
   :all-builds          ;; <-- supply your build configs here
   [{:id "devcards"
     :source-paths ["src" "test"]
     :figwheel { :devcards true } ;; <- note this
     :compiler {:main       "build-monitor.test.devcards"
                :asset-path "js/compiled/devcards/devcards_out"
                :output-to  "resources/public/js/compiled/devcards/build_monitor_devcards.js"
                :output-dir "resources/public/js/compiled/devcards/devcards_out"
                :source-map-timestamp true }}
    {:id "dev"
     :source-paths ["src"]
     :figwheel true
     :compiler {:main       "build-monitor.core"
                :asset-path "js/compiled/dev/out"
                :output-to  "resources/public/js/compiled/dev/build_monitor.js"
                :output-dir "resources/public/js/compiled/dev/out"
                :source-map-timestamp true}}
    {:id "demo"
     :source-paths ["src" "test"]
     :figwheel { :devcards true }
     :compiler {:main       "build-monitor.test.devcards"
                :asset-path "js/compiled/demo/demo_out"
                :output-to  "resources/public/js/compiled/demo/build_monitor_demo.js"
                :output-dir "resources/public/js/compiled/demo/demo_out"}}
    {:id "min"
     :source-paths ["src"]
     :compiler {:main       "build-monitor.core"
                :asset-path "js/compiled/min/out"
                :output-to  "resources/public/js/compiled/min/build_monitor.js"
                :optimizations :advanced}}]})

(defn start []
  (ra/start-figwheel! figwheel-config)
  (comment prevent figwheel from spamming the repl with a huge SystemMap))

(defn stop []
  (ra/stop-figwheel!)
  nil)

(defn restart []
  (stop)
  (ra/reload-config)
  (start))

(defn cljs
  ([] (cljs "devcards"))
  ([build-id] (ra/cljs-repl build-id)))
