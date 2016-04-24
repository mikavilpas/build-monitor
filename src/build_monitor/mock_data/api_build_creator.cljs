(ns build-monitor.mock-data.api-build-creator
  (:require [build-monitor.data-source.data-source-v2]))

;; used for creating mock apis from build-status-builder output

(defn- date [d] (.toXmlDateTime d))

(defn- commit->api-commit [commit]
  (when commit
    {:CommitterDisplayName (:last-changed-by-name commit)
     :Comment (:commit-message commit)
     :Id (:id commit)
     :Date (date (:commit-time commit))
     :AdditionalInfoUri (:additional-info-uri commit)}))

(defn- merge-candidate->api-merge-candidate [m]
  {:SourceBranch (:source-branch m)
   :TargetBranch (:target-branch m)
   :Commits (mapv commit->api-commit (:commits m))})

(defn build-status->api-build [b]
  {:Description (:build-name b)
   :Status (:success? b)
   :Id (:build-name b)
   :StartTime (date (:start-time b))
   :FinishTime (date (:finish-time b))
   :BuildNumber (:full-build-number b)
   :RunningBuild (when-let [start-time (:build-currently-running-start-time b)]
                   {:StartTime (date start-time)})
   :LatestCommit (commit->api-commit (:commit b))

   ;; cannot be reversed
   :BuildLog []

   :FirstBreakingCommit (commit->api-commit (:first-breaking-commit b))
   :LatestSuccessfulBuild (when-let [build (:latest-successful-build b)]
                            {:StartTime (date (:start-time build))
                             :FinishTime (date (:finish-time build))})

   :BuildDiagnosticsUri (:build-details-link-uri b)
   :BranchInformationUri (:branch-information-uri b)
   :MergeCandidates (mapv merge-candidate->api-merge-candidate
                          (:merge-candidates b))})

(defn serialize-build-status [b]
  (build-status->api-build b))

(defn prettify [clj-data]
  (let [spacing 2]
    (-> clj-data
        clj->js
        (js/JSON.stringify nil spacing))))
