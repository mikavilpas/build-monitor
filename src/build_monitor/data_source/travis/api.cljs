(ns build-monitor.data-source.travis.api
  (:require [cljs-http.client :as http]
            [cljs.core.async :as async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def ^:private api-base-url "https://api.travis-ci.org/")

(def ^:private base-request {:with-credentials? false
                             :accept "application/vnd.travis-ci.2+json"})

(defn- route [route]
  (+ api-base-url route))

(defn- call-api [path]
  (go (let [response (<! (http/get (route path)
                                   base-request))]
        (js->clj (:body response) :keywordize-keys true))))


(defrecord Config [user-name repo-name])
(defn- repo-name [config]
  (str (:user-name config) "/" (:repo-name config)))

(defn branches [config]
  (call-api (str "/repos/" (repo-name config) "/branches")))
