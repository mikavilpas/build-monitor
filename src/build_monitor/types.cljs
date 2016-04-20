(ns build-monitor.types
  (:require [schema.core :as s :include-macros true]
            [goog.date]))

(declare CommonError)

(def LocalDateTime "in the browser's timezone" goog.date.DateTime)
(s/defrecord Commit [id :- s/Str
                     last-changed-by-name :- s/Str
                     commit-message :- s/Str
                     commit-time :- LocalDateTime
                     additional-info-uri :- (s/maybe s/Str)])

(s/defrecord MergeCandidateInformation [source-branch :- s/Str
                                        target-branch :- s/Str
                                        commits :- [Commit]])

(def CommonError {:match-regex s/Str
                  (s/optional-key :css-class-name) s/Str
                  :display-text {:short-explanation s/Str
                                 (s/optional-key :full-explanation) s/Str}})

(s/defrecord BuildStatusErrors [build-error-log :- (s/maybe [s/Str])
                                common-build-error :- (s/maybe CommonError)])

(s/defrecord BuildStatus [build-name :- s/Str
                          success? :- (s/enum true false :build-not-found)
                          start-time :- (s/maybe LocalDateTime)
                          finish-time :- (s/maybe LocalDateTime)
                          full-build-number :- (s/maybe s/Str)
                          short-build-number :- (s/maybe s/Str)

                          build-currently-running-start-time :- (s/maybe LocalDateTime)

                          commit :- (s/maybe Commit)
                          errors :- (s/maybe BuildStatusErrors)
                          first-breaking-commit :- (s/maybe Commit)
                          latest-successful-build :- (s/maybe {:start-time LocalDateTime
                                                               :finish-time LocalDateTime})
                          build-details-link-uri :- (s/maybe s/Str)
                          branch-information-uri :- (s/maybe s/Str)
                          merge-candidates :- (s/maybe [MergeCandidateInformation])])

(def BuildName s/Str)
(def BuildNameMatrix [[BuildName]])
(def BuildMatrix [[BuildStatus]])

(def Config {:build-matrix BuildNameMatrix
             :apis {:version2 s/Str
                    (s/optional-key :recent-events) s/Str}
             :build-status-poll-timeout-ms s/Num
             :common-errors [CommonError]})
