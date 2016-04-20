(ns build-monitor.test.app.app-environment-tests
  (:require [build-monitor.app :as app]
            [build-monitor.build-monitor-loading :as monitor]
            [build-monitor.build-status-builder :as status-builder]
            [build-monitor.components.build-monitor-components :as components]
            [build-monitor.recent-events-loading :as recent-events]
            [build-monitor.test-utils :as test-utils]
            [reagent.core :as reagent])
  (:require-macros [devcards.core :as dc :refer [defcard-rg]]))

(def include-me :yep)

(def builds
  [(->> (status-builder/build "The best build" true)
        (status-builder/commit "Fix everything, also optimize" "Antti Alpha")
        (status-builder/branch-information-uri "#branch-info"))
   (->> (status-builder/build "Kitten generator site" false)
        (status-builder/commit "Increase kitten throughput, this is a really long commit message that should not make the build monitor component stretch oddly, la la la la la la la la la"
                               "Marcus Von Kitten :3")
        (status-builder/custom-error test-utils/test-failure)
        (status-builder/first-breaking-commit-mins-ago 32)
        (status-builder/current-build-started-minutes-ago 10)
        (status-builder/latest-successful-build-length 35))
   (->> (status-builder/build "Hotfixes to production" false)
        (status-builder/commit "I think this works" "Dingo Delta")
        (status-builder/custom-error test-utils/compilation-error)
        (status-builder/first-breaking-commit-mins-ago 1000))

   (->> (status-builder/build "Oops build" false)
        (status-builder/commit "Oops, the last commit was broken" "Dingo Delta")
        (status-builder/first-breaking-commit-mins-ago 28))
   (->> (status-builder/build "Where is my build?" :build-not-found))
   (->> (status-builder/build "Legacy 1.0" true)
        (status-builder/commit "commit message" "Ovadiah Frankel"))
   (->> (status-builder/build "Legacy 2.0" false)
        (status-builder/commit "commit message" "Choni Goldblum")
        (status-builder/first-breaking-commit-mins-ago 15))

   (->> (status-builder/build "Oriental Treats" false)
        (status-builder/commit "Nom nom, more test data" "Case Rondition")
        (status-builder/custom-error test-utils/file-access-error)
        (status-builder/first-breaking-commit-mins-ago 3)
        (status-builder/merge-candidates
         [(status-builder/merge-candidate
           "Kitten generator site"
           "Oriental Treats"
           (status-builder/make-commit "Try out new things"
                                       "Woot woot")
           (status-builder/make-commit "Aaw yiss, now it works"
                                       "Mr. Anderson, the son of man"))]))
   (->> (status-builder/build "Bug Blues" false)
        (status-builder/commit "I can't get anything right" "Kylo Ren")
        (status-builder/custom-error test-utils/validate-merges)
        (status-builder/first-breaking-commit-mins-ago 77)
        (status-builder/merge-candidates
         [(status-builder/merge-candidate
           "Oriental Treats"
           "Bug blues"
           (status-builder/make-commit "Change stuff"
                                       "Hon Salo"))]))])

(defonce mock-config
  {:build-matrix [["The best build"
                   "Kitten generator site"
                   "Hotfixes to production"]
                  ["Oops build"
                   "Where is my build?"
                   "Legacy 1.0"
                   "Legacy 2.0"]
                  ["Oriental Treats"
                   "Bug Blues"]]})

;; todo needs recent-events too

(defcard-rg app
  (let [builds-atom (reagent/atom {:builds builds
                                   :config mock-config})
        recent-events-atom (reagent/atom (recent-events/get-initial-state))]
    (monitor/prepare-config! builds-atom mock-config)
    (monitor/new-builds-arrived builds builds-atom)
    [app/app-component builds-atom recent-events-atom]))
