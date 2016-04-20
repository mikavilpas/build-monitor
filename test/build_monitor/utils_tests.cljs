(ns build-monitor.test.utils-tests
  (:require [cljs.test :refer-macros [is testing async]]
            [build-monitor.test-utils :as test-utils]
            [cljs.core.async :as async :refer [<! >!]]
            [build-monitor.utils :as utils]
            [schema.core :as s])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [devcards.core :as dc :refer [defcard-rg defcard deftest]]))

(def include-me :yep)

(deftest convert-js-type-test
  (testing "can convert an array"
    (is (= [1 2 3]
           (utils/convert-js-type (js/Array 1 2 3)))))

  (testing "array of arrays"
    (is (= [[1 2] [3 4]]
           (utils/convert-js-type (js/Array (js/Array 1 2)
                                            (js/Array 3 4))))))

  (testing "mixed value types in a map"
    (is (= {:a "a"
            :b [3 2]}
           (utils/convert-js-type (js-obj "a" "a"
                                          "b" (js/Array 3 2))))))

  (testing "actual data"
    (let [data (js-obj "build-matrix" (js/Array
                                       (js/Array "Build 1"
                                                 "Build 2")
                                       (js/Array "Build 3"
                                                 "Build 4")))]
      (is (= {:build-matrix [["Build 1" "Build 2"]
                             ["Build 3" "Build 4"]]}
             (utils/convert-js-type data))))))


(deftest repeated-timed-calls-test-return-given-data
  (async
   test-done
   (go (let [receive-chan (async/chan 5)
             stop-repeated-calls (utils/repeated-timed-calls
                                  100
                                  #(go (>! receive-chan "hi")))]
         (try (<! (async/timeout 600))
              (is (= ["hi" "hi" "hi" "hi" "hi"]
                     (vector (<! receive-chan)
                             (<! receive-chan)
                             (<! receive-chan)
                             (<! receive-chan)
                             (<! receive-chan))))

              (finally (stop-repeated-calls)
                       (test-done)))))))
