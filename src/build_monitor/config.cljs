(ns build-monitor.config
  (:require [schema.core :as s]
            [build-monitor.utils :as utils]
            [build-monitor.types :as types]
            [cljs.core.async :refer [<!]]

            ;; loads window.jsyaml as a global variable
            [cljsjs.js-yaml :as yaml])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(s/defn config :- types/Config [key]
  {:key key})

(defn- safe-load [data]
  (js/jsyaml.safeLoad data))

(s/defn read-config :- types/Config [data]
  (->> (safe-load data)
       utils/convert-js-type))

(defn read-config-from-url [url]
  (go (let [response (<! (utils/get-body-or-fail url))]
        (read-config response))))
