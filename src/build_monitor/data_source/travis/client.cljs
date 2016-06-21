(ns build-monitor.data-source.travis.client
  (:require [build-monitor.data-source.travis.api :as api]
            [build-monitor.time-processing :as time-processing]
            [build-monitor.types :as types]))

(defn config [user-name repo-name]
  (api/Config. user-name repo-name))

(defn- getter-fn [object]
  (fn [key]
    (get object key)))

(defn- to-time [datetime-string]
  (time-processing/date-string-to-local-date datetime-string))

(defn- parse-commit [api-commit]
  (let [get (getter-fn api-commit)
        commit (types/map->Commit {:id (get :sha)
                                   :last-changed-by-name (get :author_name)
                                   :commit-message (get :message)
                                   :commit-time (to-time (get :committed_at))
                                   ;; todo go to github
                                   :additional-info-uri "#"})]
    (types/map->BuildStatus {:build-name (get :branch)
                             :commit commit})))

(defn- parse-branch [api-branch]
  (let [get (getter-fn api-branch)]
    (types/map->BuildStatus {:start-time (to-time (get :started_at))
                             :finish-time (to-time (get :finished_at))
                             :success? (= "passed" (get :state))
                             :full-build-number (get :number)
                             :short-build-number (get :number)})))

(defn- parse-builds [api-response]
  ;; the naming in the api is unintuitive to me
  ;; todo
  (let [branches (:branches api-response)]
    (parse-branch branches)))

(defn get-builds [config]
  (-> (api/branches config)
      parse-builds))
