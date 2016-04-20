(ns build-monitor.data-source.data-source-v2
  (:require [build-monitor.data-source.common :as common]
            [build-monitor.utils :as utils]
            [build-monitor.time-processing :as time-processing]
            [build-monitor.types :as types]
            [cljs.core.async :as async :refer [<!]]
            [clojure.string :as str]
            [schema.core :as s])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def api-commit {:CommitterDisplayName s/Str
                 :Comment s/Str
                 :Id s/Str
                 :Date s/Str
                 :AdditionalInfoUri (s/maybe s/Str)
                 s/Any s/Any})

(def api-merge-candidate-information {:SourceBranch s/Str
                                      :TargetBranch s/Str
                                      :Commits [types/Commit]})

(def api-running-build {:StartTime s/Str})

(def api-build {:Description s/Str
                :Status s/Bool
                :Id s/Str
                :StartTime s/Str
                :FinishTime s/Str
                :BuildNumber s/Str
                :RunningBuild (s/maybe api-running-build)
                :LastChangedByDisplayName s/Str
                :LatestCommit api-commit
                :BuildLog [s/Str]
                :FirstBreakingCommit (s/maybe api-commit)
                :LatestSuccessfulBuild {:StartTime s/Str
                                        :FinishTime s/Str}
                :BuildDiagnosticsUri (s/maybe s/Str)
                :BranchInformationUri (s/maybe s/Str)
                :MergeCandidates (s/maybe [api-merge-candidate-information])
                s/Any s/Any})

(def parse-date utils/parse-date)

(s/defn get-short-build-number :- s/Str
  [full-build-number :- s/Str]
  (last
   (str/split full-build-number #"\.")))

(s/defn get-first-common-build-error :- (s/maybe types/CommonError)
  [build-log :- [s/Str]
   common-build-errors :- [types/CommonError]]
  (let [log (str/join build-log)]
    (first (filter #(let [pattern (:match-regex %)]
                      (re-find (re-pattern pattern)
                               log))
                   common-build-errors))))

(defn- get-commit [commit]
  (when commit
    (types/map->Commit {:last-changed-by-name (:CommitterDisplayName commit)
                        :id (:Id commit)
                        :commit-message (:Comment commit)
                        :commit-time (parse-date (:Date commit))
                        :additional-info-uri (:AdditionalInfoUri commit)})))

(defn- get-merge-candidate [data]
  (types/map->MergeCandidateInformation {:source-branch (:SourceBranch data)
                                         :target-branch (:TargetBranch data)
                                         :commits (map get-commit (:Commits data))}))

(s/defn api-build->BuildStatus :- types/BuildStatus
  [build :- api-build
   common-build-errors :- types/Config]
  ;; todo this function is way too long
  (let [get-key #(get build %)
        commit (get-key :LatestCommit)
        full-build-number (get-key :BuildNumber)]

    ;; the api sends UTC times
    (types/map->BuildStatus
     {:build-name (get-key :Description)
      :success? (get-key :Status)
      :start-time (parse-date (get-key :StartTime))
      :finish-time (parse-date (get-key :FinishTime))
      :full-build-number full-build-number
      :short-build-number (get-short-build-number full-build-number)

      :build-currently-running-start-time
      (when-let [time (-> build :RunningBuild :StartTime)]
        (parse-date time))

      :commit (get-commit commit)

      :errors (when-let [build-log (get-key :BuildLog)]
                (types/map->BuildStatusErrors
                 {:build-error-log build-log
                  :common-build-error (get-first-common-build-error build-log
                                                                    common-build-errors)}))
      :first-breaking-commit (-> (get-key :FirstBreakingCommit)
                                 get-commit)
      :latest-successful-build (when-let [b (get-key :LatestSuccessfulBuild)]
                                 {:start-time (parse-date (:StartTime b))
                                  :finish-time (parse-date (:FinishTime b))})
      :build-details-link-uri (get-key :BuildDiagnosticsUri)
      :branch-information-uri (get-key :BranchInformationUri)
      :merge-candidates (->> (get-key :MergeCandidates)
                             (map get-merge-candidate))})))

(s/defrecord DataSourceForVersion2 [url :- s/Str
                                    common-build-errors :- [types/CommonError]]
  common/DataSource
  (get-build-statuses
   [this]
   (go (let [response (<! (common/get-body url))
             builds (->> (:Builds response)
                         (map #(api-build->BuildStatus % common-build-errors)))]
         {:builds builds
          :update-time (parse-date (:UpdateTime response))}))))
