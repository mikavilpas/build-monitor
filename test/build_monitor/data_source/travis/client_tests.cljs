(ns build-monitor.data-source.travis.client-tests
  (:require [build-monitor.data-source.travis.client :as client]
            [reagent.core :as reagent]
            [build-monitor.test-utils :as test-utils])
  (:require-macros [devcards.core :as dc :refer [defcard-rg defcard deftest]]
                   [cljs.core.async.macros :refer [go]]))

(def include-me :yep)

(defn- hide-empty-keys [target-map]
  (into {} (filter (fn [[k v]]
                     (not (nil? v)))
                   target-map)))

(defcard-rg parse-commit
  (hide-empty-keys
   (client/parse-commit {:committer_name "Mika Vilpas"
                         :committer_email "mika.vilpas@gmail.com"
                         :compare_url "https://github.com/sp3ctum/evil-lispy/compare/0d14fd96bdac...af670bf37068"
                         :id 37414105
                         :author_name "Mika Vilpas"
                         :author_email "mika.vilpas@gmail.com"
                         :committed_at "2016-05-22T18:06:25Z"
                         :branch "master"
                         :sha "af670bf37068d94e4f9c0bcb5019d4c11dbf4143"
                         :message "Fix always displaying navigation help on load"})))

(defcard-rg parse-branch
  (hide-empty-keys
   (client/parse-branch {:job_ids [132109493]
                         :number "64"
                         :config {:language "emacs-lisp"
                                  :env ["EVM_EMACS=emacs-24.5-bin"]
                                  :before_install ["sudo mkdir /usr/local/evm"]
                                  :script ["./run-tests.sh"]
                                  :.result "configured"
                                  :group "stable"
                                  :dist "precise"}
                         :finished_at "2016-05-22T18:08:23Z"
                         :started_at "2016-05-22T18:07:37Z"
                         :duration 46
                         :state "passed"
                         :id 132109492
                         :pull_request false
                         :commit_id 37414105
                         :repository_id 8235269})))
