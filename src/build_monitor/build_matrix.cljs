(ns build-monitor.build-matrix
  (:require [build-monitor.types :as types]
            [schema.core :as s]))

(defn get-build-with-id [builds build-id]
  (or (first
       (filter #(= build-id (:build-name %))
               builds))
      (types/map->BuildStatus {:build-name build-id
                               :status :build-not-found})))

(s/defn place-builds-on-matrix [matrix :- types/BuildNameMatrix
                                build-matrix :- types/BuildMatrix]
  (let [builds (flatten build-matrix)]
    (for [row matrix]
      (for [build-id row]
        (get-build-with-id builds build-id)))))
