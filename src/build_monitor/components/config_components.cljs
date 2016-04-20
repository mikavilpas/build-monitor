(ns build-monitor.components.config-components
  (:require [build-monitor.components.general-components :as general-components]
            [build-monitor.storage :as storage]
            cljsjs.bootstrap
            cljsjs.react-select
            cljsjs.react-reorderable
            [clojure.string :as str]
            [reagent.core :as reagent]
            [cljs.core.async :as async]
            [reagent-modals.modals :as modals])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce saved-config-key :build-monitor-config)
(defonce config-currently-saved? (reagent/atom
                                  (storage/contains-key? saved-config-key)))

(defn delete-build [matrix [row-index build-index]]
  (update-in matrix
             [row-index]
             #(vec (keep-indexed (fn [i build-name]
                                   (when (not= build-index i)
                                     build-name))
                                 %))))

(defn add-build [build-matrix
                 new-build-name
                 row-index]
  (vec (update-in build-matrix [row-index]
                  #(conj % new-build-name))))

(defn update-build [build-matrix new-build-name build-coords]
  (assoc-in build-matrix build-coords new-build-name))




(def ^:private react-reorderable (reagent/adapt-react-class js/ReactReorderable))

(defn orderable-list-component [source-items-atom]
  (let [wrap-in-numbered-row (fn [index contents]
                               [:div {:key index}
                                contents])
        get-row-with-number (fn [item]
                              (let [row-index (int (aget item "key"))]
                                (nth @source-items-atom row-index)))]
    (let [wrapped-items (map-indexed wrap-in-numbered-row
                                     @source-items-atom)]
      (apply vector
             react-reorderable
             {:mode "list"
              :on-drop (fn [new-rows]
                         (let [rows-in-new-order (mapv get-row-with-number
                                                       new-rows)]
                           (reset! source-items-atom rows-in-new-order)))}
             wrapped-items))))




(def ^:private select-component (reagent/adapt-react-class js/Select))

(defn single-select-component [build-matrix-atom
                               build-name
                               build-coords
                               available-build-names]
  (let [initial-options (for [b (->> available-build-names
                                     distinct
                                     sort)]
                          {:value b, :label b})]
    (fn render [build-matrix-atom
                build-name
                build-coords]
      ;; overwrite bad-looking react-select styles
      (let [selection (reagent/atom build-name)]
        [:div {:style {:width "100%"
                       :font-size "small"}}
         [select-component {:options initial-options
                            :on-change (fn [new-build-name]
                                         (reset! selection new-build-name)
                                         (swap! build-matrix-atom
                                                update-build
                                                (aget new-build-name "value")
                                                build-coords))
                            :value @selection
                            :clearable false
                            :placeholder "Select build"}]]))))

(defn- build-name-editor [build-matrix-atom
                          build-coords
                          available-build-names]
  (let [build-name (get-in @build-matrix-atom build-coords)]
    [:div.horizontal
     [single-select-component build-matrix-atom build-name build-coords available-build-names]
     [:button.btn.btn-default
      {:type "button"
       :on-click #(swap! build-matrix-atom delete-build build-coords)}
      [:span.glyphicon.glyphicon-trash.pointer]]]))

(defn- build-row-component [build-matrix-atom
                            build-names
                            row-index
                            available-build-names]
  [:div.vertical {:style {:width "100%"}}
   (for [[build-index build-name] (zipmap (range) build-names)]
     [:div {:key (str "editor-row-" row-index "-build-" build-index)}
      [build-name-editor build-matrix-atom [row-index build-index] available-build-names]])
   [:button.btn.btn-default
    {:type "button"
     :on-click #(swap! build-matrix-atom add-build "" row-index)}
    [:span.glyphicon.glyphicon-plus]]])

(defn- build-row-editor [build-matrix-atom
                         available-build-names]
  [:div
   (for [[build-names row-index] (zipmap @build-matrix-atom (range))]
     [:div.horizontal.build-matrix-editor-builds
      {:key (str "config-component-row-" row-index)}
      [build-row-component
       build-matrix-atom
       build-names
       row-index
       available-build-names]])])

(defn- build-matrix-editor [build-matrix-atom
                            available-build-names]
  [:div.form-group
   [:label "Builds"]
   [build-row-editor build-matrix-atom available-build-names]])

(defn- save-button [config-atom]
  (let [saved-icon-visible (reagent/atom false)
        save #(go (storage/save :build-monitor-config @config-atom)
                  (reset! config-currently-saved? true)
                  (reset! saved-icon-visible true)
                  (async/<! (async/timeout 500))
                  (modals/close-modal!))]
    (fn []
      [:button
       {:type "button"
        :disabled @saved-icon-visible
        :class (if-not @saved-icon-visible
                 "btn btn-primary"
                 "btn btn-success")
        :on-click save}

       (if @saved-icon-visible
         [:span.glyphicon.glyphicon-ok]
         [:span.glyphicon.glyphicon-floppy-disk])])))

(defn- delete-saved-config-button []
  (let [confirm-and-delete
        (fn []
          (when (js/confirm (str "Confirm deletion! Local configuration"
                                 " will be lost and the default configuration"
                                 " will take its place."))
            (storage/delete saved-config-key)
            (reset! config-currently-saved? false)
            (modals/close-modal!)))]
    [:div
     [:button.btn.btn-warning
      {:on-click confirm-and-delete
       :disabled (not @config-currently-saved?)}
      "Delete local config"]]))

(defn dialog-contents [config-atom
                       available-build-names]
  (let [build-matrix-atom (reagent/cursor config-atom [:build-matrix])]
    [:div.well.vertical
     [build-matrix-editor build-matrix-atom available-build-names]
     [:div.horizontal.align-this-center
      [save-button config-atom]
      [delete-saved-config-button]]]))

(defn config-component [config-atom
                        available-build-names]
  [:div.align-this-center
   [modals/modal-window]
   [general-components/tooltip
    (reagent/as-element [:span (if @config-currently-saved?
                                 "Modify saved settings"
                                 "Create local settings")])
    [:div.config-component
     [:span.config-icon.glyphicon.glyphicon-cog.pointer
      {:on-click #(modals/modal! [dialog-contents
                                  config-atom
                                  available-build-names]
                                 {:size :sm})
       :style (when @config-currently-saved?
                {:color "lightgreen"})}]]
    :underline? false]])

(defn merge-config!
  "config-atom should contain the default config from the server"
  [config-atom]
  (try
    (when-let [custom-config (storage/get-value saved-config-key)]
      ;; custom-config will take preference
      (swap! config-atom (fn [default-config]
                           (merge default-config (or custom-config {})))))
    (catch js/Error e
      (js/console.log "Error in merge-config!: " e)
      (js/console.log "Removing invalid local config to recover from this")
      (storage/delete saved-config-key)
      (js/console.log "merge-config!: Done.")))
  config-atom)
