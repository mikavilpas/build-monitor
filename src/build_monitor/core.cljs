(ns build-monitor.core
  (:require [build-monitor.app :as app]))

(defn main []
  ;; conditionally start the app based on whether the #main-app-area
  ;; node is on the page
  (when-let [node (.getElementById js/document "main-app-area")]
    (app/start-app node)))

(main)
