(ns build-monitor.components.recent-events-components
  (:require [build-monitor.components.general-components :as general-components]))

(defn- explanation-message [event]
  [:div.horizontal.align-center.explanation-message
   (:explanation-message event)])

(defn- labels-component [event]
  (when-let [labels (:labels event)]
    [:div.horizontal.recent-event-labels
     (for [label labels]
       [:span.recent-event-label.label.label-success
        {:key label}
        label])]))

(defn- header-component [event]
  [:div.vertical.recent-event-header
   [:div.horizontal.space-between
    [:div.no-padding.col-xs-8
     [:span.icon.glyphicon.glyphicon-user]
     (:triggered-by-user event)]
    [general-components/how-long-ago-label (:time event)]]
   [labels-component event]])

(defn recent-event [event]
  [:div.recent-event-border
   {:class (condp = (:event-type event)
             "BuildStarted" "blue"
             "Commit" "orange"
             "")}
   [:a.list-group-item.recent-event-link
    (merge {:target "_blank"}
           (when-let [url (:additional-info-uri event)]
             {:href url}))
    [:div.vertical.recent-event-contents
     [header-component event]
     [explanation-message event]]]])

(defn sort-events [recent-events]
  (reverse
   (sort-by #(-> % :time .getTime)
            recent-events)))

(defn recent-events-list [recent-events]
  (let [sorted-events (sort-events recent-events)]
    [:div.list-group
     (for [event sorted-events]
       [:div {:key (str (:time event)
                        (:triggered-by-user event)
                        (subs (:explanation-message event) 0 10))}
        [recent-event event]])]))
