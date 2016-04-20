(ns build-monitor.build-status-builder
  (:require [build-monitor.types :as types]
            [build-monitor.data-source.data-source-v2 :as data]
            [cljs-time.core :as time]
            [cljs-time.local :as time-local]
            [build-monitor.time-processing :as time-processing]))

(defn current-build-started-minutes-ago [minutes build]
  (assoc build
         :build-currently-running-start-time
         (time/minus (time-processing/now)
                     (time/minutes minutes))))

(defn- get-start-date []
  (-> (time-local/local-now)
      (time/minus (time/days 1))))

(defn- get-end-date []
  (let [minutes (rand-int 90)
        time (-> (time-local/local-now)
                 (time/minus (time/days 1))
                 (time/plus (time/minutes minutes)))]
    time))

(defn latest-successful-build-length [minutes build]
  (let [start (get-start-date)
        finish (time/plus start (time/minutes minutes))]
    (assoc build :latest-successful-build {:start-time start
                                           :finish-time finish})))

(defn build
  "start building with ->> and this"
  [build-name success?]
  (let [full-build-number (str build-name "." (rand-int 1000))]
    (types/map->BuildStatus {:build-name build-name
                             :success? success?
                             :start-time (get-start-date)
                             :finish-time (get-end-date)
                             :build-details-link-uri "about:blank"
                             :full-build-number full-build-number
                             :short-build-number (data/get-short-build-number full-build-number)})))

(defn start-and-finish-time [start-time finish-time build]
  (assoc build
         :start-time start-time
         :finish-time finish-time))

(defn- make-commit [commit-message
                    last-changed-by-name]
  (types/map->Commit {:last-changed-by-name last-changed-by-name
                      :commit-message commit-message
                      :id (rand-int 999)
                      :commit-time (time-processing/now)
                      :additional-info-uri "#"}))

(defn commit [commit-message
              last-changed-by-name
              build]
  (assoc build
         :commit (make-commit commit-message last-changed-by-name)))

(defn merge-candidates [candidates build]
  (assoc build :merge-candidates candidates))

(defn merge-candidate [source-branch target-branch & commits]
  (types/map->MergeCandidateInformation {:source-branch source-branch
                                         :target-branch target-branch
                                         :commits commits}))

(defn branch-information-uri [uri build]
  (assoc build :branch-information-uri uri))

(defn first-breaking-commit-mins-ago [minutes build]
  (let [c (-> (make-commit "Commit message" "Committer name")
              (update-in [:commit-time]
                         #(time/minus % (time/minutes minutes))))]
    (assoc build :first-breaking-commit c)))

(defn custom-error [error build]
  (assoc build :errors
         (types/map->BuildStatusErrors {:common-build-error error})))
