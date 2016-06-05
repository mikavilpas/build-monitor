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
                :LatestCommit api-commit
                :BuildLog [s/Str]
                :FirstBreakingCommit (s/maybe api-commit)
                :LatestSuccessfulBuild {:StartTime s/Str
                                        :FinishTime s/Str}
                :BuildDiagnosticsUri (s/maybe s/Str)
                :BranchInformationUri (s/maybe s/Str)
                :MergeCandidates (s/maybe [api-merge-candidate-information])
                s/Any s/Any})

(s/defn api-build->BuildStatus :- types/BuildStatus [])

(defn get-short-build-number [& args])
(defn ->DataSourceVersion2 [& args])
