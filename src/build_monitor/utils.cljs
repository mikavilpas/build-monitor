(ns build-monitor.utils
  (:require [cljs.core.async :as async :refer [<! >!]]
            [build-monitor.time-processing :as time-processing]
            [cljs-http.client :as http])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn get-body-or-fail [url]
  (go (let [response (<! (http/get url))
            ok 200]
        (if-not (= ok (:status response))
          (throw (js/Error. (str "Error getting " url "."
                                 " Got response " response)))
          (js->clj (:body response)
                   :keywordize-keys true)))))

(defmulti convert-js-type type)

(defmethod convert-js-type :default [x]
  x)

(defmethod convert-js-type js/Array [x]
  (mapv convert-js-type x))

(defmethod convert-js-type js/Object [object]
  (into {}
        (for [k (js-keys object)]
          (let [contents (aget object k)]
            [(keyword k) (convert-js-type contents)]))))


(defn repeated-timed-calls [timeout-ms f]
  (f)
  (let [stopping-chan (async/chan)]
    (go-loop []
      (let [[value channel] (async/alts! [(async/timeout timeout-ms)
                                          stopping-chan])]
        (when-not (= channel stopping-chan)
          (f)
          (recur))))
    (fn stop-loop []
      (go (>! stopping-chan :stop-pls)))))

(defn parse-date [date-string]
  (time-processing/date-string-to-local-date date-string))
