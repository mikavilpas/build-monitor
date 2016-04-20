(ns build-monitor.components.build-monitor-components
  (:require [build-monitor.build-matrix :as build-matrix]
            [build-monitor.components.general-components :as general-components]
            [build-monitor.special-effects.spinner :as spinner]
            [build-monitor.time-processing :as time-processing]
            [build-monitor.types :as types]
            cljsjs.bootstrap
            [clojure.string :as str]
            [goog.string :as gstring]
            [reagent.core :as reagent]
            [schema.core :as s]
            [reagent-modals.modals :as modals]))

(def tooltip general-components/tooltip)

(defn- build-currently-running-component [build]
  (when (:build-currently-running-start-time build)
    [:div.align-this-center
     [spinner/spinner-component
      (spinner/display-options (spinner/width "60px")
                               (spinner/height "60px"))
      (spinner/spinner-options (spinner/color "gray"))]]))

(defn build-time-component [build]
  (when-let [finish-time (:finish-time build)]
    (let [duration (time-processing/duration (:start-time build)
                                             finish-time)]
      [:div.build-time
       [:span.glyphicon.glyphicon-time.icon]
       [:span.build-time-minutes duration]])))

(defn- build-number-component [build]
  (let [number (:full-build-number build)
        short-number (:short-build-number build)]
    [:div.build-number
     (if (nil? number)
       general-components/non-breaking-space
       [:div
        [:span.icon.glyphicon.glyphicon-tag]
        [:span
         [tooltip (reagent/as-element [:div.vertical
                                       "Build number:"
                                       number
                                       "(Click to open build log)"])
          [:a.text-like-link {:href (:build-details-link-uri build)
                              :target "_blank"}
           [:span
            [:span.hash "#"] short-number]]]]])]))

(defn build-interval-component [build]
  (when-let [finish-time (:finish-time build)]
    [:div.vertical
     [:span
      [:span.icon.glyphicon.glyphicon-check]
      [tooltip
       (reagent/as-element
        [:div.vertical
         "Build started:" (time-processing/human-readable-time (:start-time build))
         "Build finished:" (time-processing/human-readable-time finish-time)])
       [:span (time-processing/how-long-ago finish-time
                                            @time-processing/current-time-atom)]]]]))

(defn- commit-message-component [build]
  (when-let [commit-message (:commit-message build)]
    [:div.build-additional-info.horizontal
     {:title commit-message}
     [:span.icon.glyphicon.glyphicon-comment]
     commit-message]))

(s/defn progress-bar [explanation-content
                      progress-percentage :- s/Int]
  [:div.build-progress-bar
   [:div.vertical.align-center
    explanation-content
    [:div.background
     [:div.foreground
      {:style {:width (str (min progress-percentage 100) "%")}}
      (let [overtime-percentage (-> (- progress-percentage 100)
                                    (max 0)
                                    (min 100))]
        [:div.overtime
         {:style {:width (str overtime-percentage "%")}}])]]]])

(defn build-progress-bar [build]
  (when (and (:build-currently-running-start-time build)
             (:latest-successful-build build))
    (let [old-build (vector (-> build :latest-successful-build :start-time)
                            (-> build :latest-successful-build :finish-time))
          build-start (:build-currently-running-start-time build)
          current-build (vector build-start (time-processing/now))
          completion-time (time-processing/estimated-completion-time
                           old-build
                           build-start)]

      [progress-bar [tooltip
                     (reagent/as-element
                      [:div.vertical
                       "The build should be completed at"
                       (time-processing/human-readable-time completion-time)])
                     (time-processing/how-long-to @time-processing/current-time-atom
                                                  completion-time)]

       (time-processing/percentage-of-time current-build old-build)])))

(defn build-broken-for-component [first-breaking-commit]
  (when-let [finish-time (:commit-time first-breaking-commit)]
    [:span.label.label-danger.build-broken-for
     [tooltip
      (reagent/as-element [:div.vertical
                           "The first breaking commit"
                           "was committed at"
                           (time-processing/human-readable-time
                            finish-time)])
      [:div.vertical
       "Broken for"
       (time-processing/human-readable-duration finish-time
                                                @time-processing/current-time-atom)]]]))

(defn build-info-component [build current-time-atom]
  [:div.build-info-background
   [:div.build-info-content.vertical
    [build-number-component build]
    [build-time-component build]
    [build-interval-component build current-time-atom]
    (when (= false (:success? build))
      [build-broken-for-component (:first-breaking-commit build)])
    [build-currently-running-component build]
    [build-progress-bar build]]])

(defn- last-changed-by-name-component [commit]
  (let [name (:last-changed-by-name commit)]
    [:div.build-additional-info
     (when-not (empty? name)
       [:div.build-additional-info.horizontal
        [:span.icon.glyphicon.glyphicon-user]
        name])]))

(defn- common-error-label-component [common-build-error]
  (when common-build-error
    (let [display-text (-> common-build-error
                           :display-text)]
      [:span.common-error-label.label.label-default
       (if-let [full-explanation (:full-explanation display-text)]
         [tooltip
          (reagent/as-element [:span full-explanation])
          (:short-explanation display-text)
          :placement "bottom"]
         (:short-explanation display-text))])))

(defn- plural-s-letter [coll]
  (when (> (count coll) 1)
    "s"))

(defn- candidates-count-text [commits]
  (let [first-committer (-> (first commits) :last-changed-by-name)
        committers (set (map :last-changed-by-name commits))
        only-committer? (= 1 (count committers))]
    [:span.vertical
     (str (count commits) " pending merge"
          (plural-s-letter commits))
     (str " from " first-committer)
     (when-not only-committer?
       (str " and " (dec (count committers))
            " other" (plural-s-letter (rest committers))))]))

(defn- build-status-commit-component [commit]
  (general-components/linkify
   (:additional-info-uri commit)
   (reagent/as-element
    [:div
     [last-changed-by-name-component commit]
     [commit-message-component commit]])))

(defn- commit-id-component [commit]
  (when-let [id (:id commit)]
    [:div.build-additional-info
     [:div.build-additional-info.horizontal
      [:span.icon.glyphicon.glyphicon-tag]
      id]]))

(defn- merge-candidate-commit-component [commit]
  [:div.horizontal.space-between
   [:div
    [commit-id-component commit]
    [last-changed-by-name-component commit]
    [commit-message-component commit]]
   [general-components/how-long-ago-label (:commit-time commit)]])

(defn- get-commits [merge-candidates]
  (->> merge-candidates
       (map :commits)
       flatten
       distinct
       (sort-by (comp str :commit-time))))

(defn merge-candidates-dialog-contents [merge-candidates]
  [:div.merge-candidates-dialog-contents
   [:div.well
    (for [candidate merge-candidates]
      [:div.vertical
       {:key (str (:source-branch candidate)
                  (:target-branch candidate))}
       [:div.heading.horizontal.space-between
        [:span.align-this-center
         (str "Commits from "
              (:source-branch candidate)
              " (oldest first)")]
        [:span.label.label-success
         (count (:commits candidate))]]
       [:div.list-group
        (for [commit (:commits candidate)]
          [:a.list-group-item
           {:key (str (:commit-message commit)
                      (:last-changed-by-name commit)
                      (:commit-time commit))
            :href (:additional-info-uri commit)
            :target "_blank"}
           [merge-candidate-commit-component commit]])]])]])

(defn- merge-candidates-component [build]
  (when-let [candidates (:merge-candidates build)]
    (let [commits (get-commits candidates)]
      (when (not-empty commits)
        [:span.merge-candidates-label.label.label-default.pointer
         {:on-click #(modals/modal! [merge-candidates-dialog-contents candidates])}
         (candidates-count-text commits)]))))

(defn- build-name-component [build]
  [:div.build-status-left
   [:div.vertical
    (general-components/linkify (:branch-information-uri build)
                                (:build-name build))
    [common-error-label-component (-> build
                                      :errors
                                      :common-build-error)]
    [merge-candidates-component build]]
   [build-status-commit-component (:commit build)]])

(defn get-build-status-class [build]
  (if-let [common-build-error (-> build
                                  :errors
                                  :common-build-error)]
    (str/join " "
              ["common-build-error"
               (:css-class-name common-build-error)])

    (condp = (:success? build)
      true "success"
      false "failure"
      "not-found")))

(defn build-status [build]
  (when-not (empty? build)
    [:div.horizontal.build-status.space-between
     {:class (get-build-status-class build)
      :key (str (:build-name build))}
     [build-name-component build]
     [build-info-component build]]))

(s/defn monitor [build-statuses :- types/BuildMatrix]
  [:div#monitor
   (for [[row-index builds] (zipmap (range) build-statuses)]
     [:div.horizontal
      {:key (str "build-row." row-index)}
      (for [[build-index build] (zipmap (range) builds)]
        (with-meta
          [build-status build]
          {:key (str "monitor." row-index build-index)}))])])

(defn monitor-from-atom [state-atom]
  (let [build-matrix (build-matrix/place-builds-on-matrix (-> @state-atom :config :build-matrix)
                                                          (:builds @state-atom))]
    [monitor build-matrix]))
