(ns build-monitor.test.components.monitor-components-tests
  (:require [build-monitor.build-status-builder :as status-builder]
            [build-monitor.components.build-monitor-components :as components]
            [reagent-modals.modals :as modals]
            [build-monitor.test-utils :as test-utils]
            [build-monitor.time-processing :as time-processing]
            [cljs-time.core :as time]
            [clojure.string :as str]
            [reagent.core :as reagent])
  (:require-macros [devcards.core :as dc :refer [defcard-rg]]))

(def include-me :yep)

(defcard-rg merge-candidates-dialog-contents
  (fn [state _]
    [components/merge-candidates-dialog-contents @state])
  (let [candidates [(status-builder/merge-candidate
                     "Oriental Treats"
                     "Bug blues"
                     (status-builder/make-commit "Change stuff"
                                                 "Netsky")
                     (status-builder/make-commit
                      (str/join (repeat 10 "Really long message"))
                      "Fox Stevenson"))
                    (status-builder/merge-candidate
                     "some-other-branch"
                     "Bug blues"
                     (status-builder/make-commit "Fix build"
                                                 "Margarine Maria")
                     (status-builder/make-commit "Break build"
                                                 "Monica Milk"))]]
    (reagent/atom candidates))
  {:inspect-data true})

(defcard-rg build-status-block
  [:div
   ;; this abstraction doesn't really work. it inherits styles if it's placed in a span
   [modals/modal-window]
   [:div.row "success"
    [components/build-status
     (->> (status-builder/build "Build successful" true)
          (status-builder/commit "Commit message" "Committer name"))]]

   [:div.row "failure"
    [components/build-status
     (->> (status-builder/build "Build with a very long name yep yep yep yep" false)
          (status-builder/commit "Commit message" "Committer name")
          (status-builder/first-breaking-commit-mins-ago 11))]]

   [:div.row "not found"
    [components/build-status
     (->> (status-builder/build "Lost build" :build-not-found))]]

   [:div.row "This build has passed but has pending merges"
    [components/build-status
     (->> (status-builder/build "Build 123.45" false)
          (status-builder/commit "commit message" "last-changed-by-name")
          (status-builder/custom-error test-utils/validate-merges)
          (status-builder/first-breaking-commit-mins-ago 600)
          (status-builder/merge-candidates
           [(status-builder/merge-candidate
             "Oriental Treats"
             "Bug blues"
             (status-builder/make-commit "Change stuff"
                                         "Netsky")
             (status-builder/make-commit "Change stuff"
                                         "Fox Stevenson"))
            (status-builder/merge-candidate
             "some-other-branch"
             "Bug blues"
             (status-builder/make-commit "Fix build"
                                         "Margarine Maria")
             (status-builder/make-commit "Break build"
                                         "Monica Milk"))]))]]

   [:div.row
    [components/build-status
     (->> (status-builder/build "Build 123.45" false)
          (status-builder/commit "commit message" "last-changed-by-name")
          (status-builder/custom-error test-utils/test-failure)
          (status-builder/first-breaking-commit-mins-ago 14))]]

   [:div.row
    [components/build-status
     (->> (status-builder/build "Build 123.45" false)
          (status-builder/commit "commit message" "last-changed-by-name")
          (status-builder/custom-error test-utils/file-access-error)
          (status-builder/first-breaking-commit-mins-ago 14))]]

   [:div.row
    "this build should also display indication that the build is currently running"
    [components/build-status
     (->> (status-builder/build "Build 123.45" false)
          (status-builder/commit "commit message" "last-changed-by-name")
          (status-builder/custom-error test-utils/compilation-error)
          (status-builder/first-breaking-commit-mins-ago 14)
          (status-builder/current-build-started-minutes-ago 10))]]])

(defn tooltip-test-component []
  [:div.horizontal 
   [components/tooltip
    (reagent/as-component [:div "The time is "
                           (time-processing/human-readable-time
                            @time-processing/current-time-atom)])
    [:a "hover me"]]])

(defcard-rg tooltip
  [tooltip-test-component])

(defcard-rg progress-bar
  [:div.vertical
   [components/progress-bar "20 minutes" 0]
   [components/progress-bar "19 minutes" 1]
   [components/progress-bar "15 minutes" 25]
   [components/progress-bar "a few seconds" 99]
   [components/progress-bar "a few seconds" 100]

   [:div
    "these are overtime:"
    [components/progress-bar "" 150]
    [components/progress-bar "" 180]
    [components/progress-bar "" 198]]

   [:div
    "this build should also display textual indication that the build has gone overtime"
    [components/build-status
     (->> (status-builder/build "Build 123.45" false)
          (status-builder/commit "commit message" "last-changed-by-name")
          (status-builder/custom-error test-utils/compilation-error)
          (status-builder/current-build-started-minutes-ago 60)
          (status-builder/latest-successful-build-length 35))]]

   [components/build-status
    (->> (status-builder/build "Build 123.45" false)
         (status-builder/commit "commit message" "last-changed-by-name")
         (status-builder/custom-error test-utils/compilation-error)
         (status-builder/current-build-started-minutes-ago 11)
         (status-builder/latest-successful-build-length 30))]])

(defcard-rg build-broken-for-component-test
  [:div.horizontal
   ;; have the component be in the center so the tooltip can be seen
   [:div {:style {:width "200px"}}]
   [components/build-broken-for-component
    (:first-breaking-commit
     (status-builder/first-breaking-commit-mins-ago 13 {}))]])
