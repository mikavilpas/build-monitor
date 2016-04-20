(ns build-monitor.data-source.common
  (:require [build-monitor.types :as types]
            [cljs-http.client :as http]
            [cljs.core.async :as async :refer [<! >!]]
            [schema.core :as s])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn get-body [url]
  (go (let [response (<! (http/get url {:with-credentials? false
                                        :accept "application/json"}))]
        (js->clj (:body response) :keywordize-keys true))))

(defprotocol DataSource
  (get-build-statuses [this]))
