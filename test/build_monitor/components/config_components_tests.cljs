(ns build-monitor.components.config-components-tests
  (:require [build-monitor.components.config-components :as config-components]
            [build-monitor.config :as config]
            [build-monitor.storage :as storage]
            [build-monitor.test-utils :as test-utils]
            [cljs.core.async :as async :refer [<!]]
            [cljs.test :refer-macros [is]]
            [reagent.core :as reagent])
  (:require-macros
   [cljs.core.async.macros :refer [go]]
   [devcards.core :as dc :refer [defcard-rg deftest]]))

(def include-me :yep)

(def test-config test-utils/test-config)
(defonce available-build-names (reagent/atom (->> (:build-matrix @test-config)
                                                  flatten
                                                  (into []))))

(defcard-rg orderable-list-component
  (fn [state _]
    [:div "Drag these around and watch the input atom change"
     [config-components/orderable-list-component state]])
  (reagent/atom [[:span.label.label-default "One"]
                 [:span.label.label-info "Two"]
                 [:span.label.label-warning "Three"]])
  {:inspect-data true})

(defcard-rg config-component
  [:div
   "This card loads and saves data like the real component"
   [config-components/config-component
    (config-components/merge-config! test-config)
    @available-build-names]])

(defcard-rg dialog-contents
  (fn [state _]
    [:div "This example always uses the example config"
     " and will not display saved changes"
     [:div {:style {:width "50%"}}
      [config-components/dialog-contents state @available-build-names]]])
  test-config
  {:inspect-data true})

(defcard-rg test-config-status
  ;; ie 11 crashes without the deref
  @test-config)

(deftest delete-build
  (is (= [["a" "c"]]
         (config-components/delete-build [["a" "b" "c"]]
                                         [0 1]))))

(deftest add-build
  (is (= [["a" "b" "c"]]
         (let [row-index 0]
           (config-components/add-build [["a" "b"]]
                                        "c"
                                        row-index)))))

(deftest merge-config!
  (let [config-atom (reagent/atom {:build-matrix [["Build 1"]
                                                  ["Build 2"]]
                                   ;; let's pretend the server has
                                   ;; added a new setting after the
                                   ;; user has saved his local config
                                   :new-setting true})]
    (is (= {:build-matrix [["Build 2" "My Custom Build"]]
            :new-setting true}
           (let [config-key config-components/saved-config-key
                 original-config (storage/get-value config-key)]
             (try
               (storage/save config-key {:build-matrix [["Build 2" "My Custom Build"]]})
               (config-components/merge-config! config-atom)

               (catch js/Error e)
               ;; try not to mess the test environment up
               (finally
                 (storage/save config-key original-config)))

             @config-atom)))))
