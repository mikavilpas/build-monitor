(ns build-monitor.time-processing-tests
  (:require [build-monitor.time-processing :as time-processing]
            [cljs.test :refer-macros [is]]
            [build-monitor.utils :as utils]
            [clojure.string :as str]
            [cljs-time.coerce :as time-coerce]
            [cljs-time.extend :as time-extend]
            [cljs-time.core :as time]
            [cljs-time.local :as time-local]
            [schema.core :as s]
            [clojure.string :as str]
            [reagent.core :as reagent])
  (:require-macros [devcards.core :as dc :refer [deftest defcard defcard-rg]]))

(def include-me :yep)

(def utc-date time-coerce/to-date-time)

(deftest duration
  (is (= "3 min"
         (time-processing/duration (utc-date "2015-12-24T00:00:00")
                                   (utc-date "2015-12-24T00:03:00"))))
  (is (= "59 min"
         (time-processing/duration (utc-date "2015-12-24T00:00:00")
                                   (utc-date "2015-12-24T00:59:00"))))
  (is (= "89 min"
         (time-processing/duration (utc-date "2015-12-24T00:00:00")
                                   (utc-date "2015-12-24T01:29:00")))))

(deftest how-long-ago-test
  (is (= "3 minutes ago"
         (time-processing/how-long-ago (utc-date "2015-12-24T00:00:00")
                                       (utc-date "2015-12-24T00:03:00"))))
  (is (= "44 minutes ago"
         (time-processing/how-long-ago (utc-date "2015-12-24T00:00:00")
                                       (utc-date "2015-12-24T00:44:00"))))
  (is (= "an hour ago"
         (time-processing/how-long-ago (utc-date "2015-12-24T00:00:00")
                                       (utc-date "2015-12-24T00:45:00")))))

(deftest human-readable-date
  (is (= "Thursday 07.01.2016   19:41:55"
         (time-processing/human-readable-time
          (utc-date "2016-01-07T19:41:55Z")))))

(defcard-rg time-formatting
  [:p "See the "
   [:a {:href "http://andrewmcveigh.github.io/cljs-time/uberdoc.html"} "cljs.time uberdoc"]
   " for documentation and examples"])

(deftest converts-api-utc-time-to-local-time
  (is (= true
         (str/includes?
          (-> "2016-01-15T13:01:58.113Z"
              time-processing/date-string-to-local-date
              time-processing/human-readable-time)
          "15:01:58"))))

(deftest parses-datetimes-correctly
  (is (= "Friday 15.01.2016   09:58:27"
         (-> "2016-01-15T07:58:27.22Z"
             time-processing/date-string-to-local-date
             time-processing/human-readable-time)))
  (is (= "Friday 15.01.2016   10:37:27"
         (-> "2016-01-15T08:37:27.467Z"
             time-processing/date-string-to-local-date
             time-processing/human-readable-time))))

(deftest how-long-to-test
  (is (= "in an hour"
         (time-processing/how-long-to (utc-date "2015-12-24T12:00:00")
                                      (utc-date "2015-12-24T13:00:00"))))
  (is (= "in 30 minutes"
         (time-processing/how-long-to (utc-date "2015-12-24T12:30:00")
                                      (utc-date "2015-12-24T13:00:00"))))
  (is (= "in a minute"
         (time-processing/how-long-to (utc-date "2015-12-24T12:59:00")
                                      (utc-date "2015-12-24T13:00:00")))))

(deftest percentage-of-time-test
  (is (= 100
         (time-processing/percentage-of-time
          [(utc-date "2016-01-01T00:00:00")
           (utc-date "2016-01-01T00:01:00")]

          [(utc-date "2016-01-01T00:00:00")
           (utc-date "2016-01-01T00:01:00")])))

  (is (= 0
         (time-processing/percentage-of-time
          [(utc-date "2016-01-01T00:00:00")
           (utc-date "2016-01-01T00:00:00")]

          [(utc-date "2016-01-01T00:00:00")
           (utc-date "2016-01-01T00:01:00")])))

  (is (= 50
         (time-processing/percentage-of-time
          [(utc-date "2016-01-01T00:00:00")
           (utc-date "2016-01-01T00:05:00")]

          [(utc-date "2016-01-01T00:00:00")
           (utc-date "2016-01-01T00:10:00")]))))

(deftest estimated-completion-time-test
  ;; date comparison is broken. I'd like to compare the result to a
  ;; (utc-date "2434...") call but it won't do
  (is (= "Friday 01.01.2016   00:05:00"
         (-> (time-processing/estimated-completion-time
              [(utc-date "2016-01-01T00:00:00")
               (utc-date "2016-01-01T00:05:00")]
              (utc-date "2016-01-01T00:00:00"))
             (time-processing/human-readable-time)))))
