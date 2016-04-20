(ns build-monitor.special-effects.spinner
  (:require [cljsjs.spin]
            [cljs.core.async :as async :refer [<! >!]]
            [schema.core :as s]
            [reagent.core :as reagent])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def base-options {:top "50%"
                   :left "50%"
                   :opacity 0
                   :trail 90})

(defn spinner-component [display-options
                         spinner-options]
  (let [spinner-id (gensym)]
    (reagent/create-class
     {:component-did-mount #(let [spinner-node (js/document.getElementById spinner-id)]
                              (doto (js/Spinner. (clj->js (merge base-options spinner-options)))
                                (.spin spinner-node)))
      :display-name (str "spinner:" spinner-id)
      :reagent-render
      (fn []
        [:div.spinner-container {:id spinner-id
                                 :style {:height (:height display-options)
                                         :position "relative"
                                         :width (:width display-options)}}])})))
(defn display-options [& options]
  (apply merge options))
(defn height [css-height] {:height css-height})
(defn width [css-width] {:width css-width})

(defn spinner-options [& options]
  (apply merge base-options (merge options)))
(defn scale [s] {:scale s})
(defn rounds-per-second [rps] {:speed rps})
(defn delay-ms [ms] (rounds-per-second (/ 1 (/ ms 1000))))
(defn lines [n] {:lines n})
(defn color [css-color] {:color css-color})
