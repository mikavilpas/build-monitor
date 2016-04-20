(ns build-monitor.components.general-components
  (:require [build-monitor.time-processing :as time-processing]
            cljsjs.bootstrap
            [goog.string :as gstring]
            [reagent.core :as reagent]))

(defn linkify [url content]
  (if (not-empty url)
    [:a {:href url
         :target "_blank"}
     content]
    content))

(defn tooltip
  "Create a bootstrap tooltip. tooltip-content must be a React
  element. Create one with e.g. reagent/as-element"
  [tooltip-content, visible-element, & {:keys [placement underline?]
                                        :or {placement "left"
                                             underline? true}}]
  (reagent/create-class
   {:component-did-mount
    (fn [this]
      (let [dom-node (reagent/dom-node this)]
        (-> (js/$ dom-node) .tooltip)))

    :reagent-render
    (fn [tooltip-content, visible-element, & {:keys [placement underline?]
                                              :or {placement "left"
                                                   underline? true}}]
      (let [s (reagent/render-to-string tooltip-content)]
        [:span {:data-toggle "tooltip"
                :data-placement placement
                :data-html true
                :data-title s
                :class (when underline?
                         "has-tooltip")
                :data-original-title s}
         visible-element]))}))

(defn how-long-ago-label [time]
  [:div.how-long-ago
   [tooltip
    (reagent/as-element [:div (time-processing/human-readable-time time)])
    [:span.label.label-default
     (time-processing/how-long-ago time)]
    :underline? false]])

(def non-breaking-space (gstring/unescapeEntities "&nbsp;"))
