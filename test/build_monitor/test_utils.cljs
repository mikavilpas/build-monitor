(ns build-monitor.test-utils
  (:require [cljs.pprint]
            [build-monitor.data-source.data-source-v2 :as data]
            [reagent.core :as reagent]
            [build-monitor.config :as config]
            [build-monitor.time-processing :as time-processing]
            [build-monitor.types :as types]
            [cljs-time.core :as time]
            [cljs-time.local :as time-local]
            [build-monitor.types :as types]
            [build-monitor.utils :as utils]
            [schema.core :as s]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn pprint [data]
  (with-out-str (cljs.pprint/pprint data)))

(defn channel->atom
  "Allows showing the first channel result in a devcard:

  (defcard foo
    (channel->atom (go :hello)))"
  [channel]
  (let [atom (reagent/atom :empty)]
    (go (reset! atom (<! channel)))
    atom))

(defn observe-value-with-timeout [timeout-ms observe-fn]
  (let [result (reagent/atom nil)
        stop (utils/repeated-timed-calls timeout-ms
                                         #(reset! result (observe-fn)))]
    (reagent/create-class
     {:reagent-render (fn [&_]
                        [:pre (with-out-str
                                (cljs.pprint/pprint @result))])
      :component-will-unmount #(stop)})))

(s/def validate-merges :- types/CommonError
  {:match-regex "ValidateMerges",
   :display-text {:short-explanation "Pending merges",
                  :full-explanation
                  "There are merge candidates to the target branch. Merge or discard them and try again."},
   :css-class-name "validate-merges"})

(s/def test-failure :- types/CommonError
  {:match-regex "Test failure",
   :display-text {:short-explanation "Test failure"},
   :css-class-name "test-failure"})

(s/def file-access-error :- types/CommonError
  {:match-regex "The process cannot access the file",
   :display-text {:short-explanation "Cannot access file"
                  :full-explanation "Some shared file on the build server cannot be accessed"},
   :css-class-name "file-access-error"})

(s/def unauthorized-access-error :- types/CommonError
  {:match-regex "Access to the path",
   :display-text {:short-explanation "Cannot access file"
                  :full-explanation "Some shared file on the build server cannot be accessed"},
   :css-class-name "file-access-error"})

(s/def compilation-error :- types/CommonError
  {:match-regex "Building solution.*?failed, see output for further details",
   :display-text {:short-explanation "Compilation error"},
   :css-class-name "compilation-error"})

(def all-common-errors
  [compilation-error, validate-merges, test-failure, file-access-error])

(defn get-start-date []
  (-> (time-local/local-now)
      (time/minus (time/days 1))))

(defn get-end-date []
  (let [minutes (rand-int 90)
        time (-> (time-local/local-now)
                 (time/minus (time/days 1))
                 (time/plus (time/minutes minutes)))]
    time))


(def test-config
  (channel->atom (go (<! (config/read-config-from-url
                          (str "/test-config.yaml?v=" (name (gensym))))))))
