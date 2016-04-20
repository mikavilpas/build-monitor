(ns build-monitor.build-matrix-tests
  (:require [build-monitor.build-matrix :as build-matrix]
            [build-monitor.components.build-monitor-components :as build-monitor-components]
            [build-monitor.build-status-builder :as status-builder]
            [build-monitor.test-utils :as test-utils]
            [cljs.test :refer-macros [is testing]]
            [clojure.walk :as walk])
  (:require-macros [devcards.core :as dc :refer [defcard-rg deftest]]))

(def include-me :yep)

(defn build [name success?]
  (status-builder/build name success?))

(def builds [[(build "C" true) (build "D" false) (build "E" true)]
             [(build "A" true) (build "B" false) (build "F" false)]])

(deftest get-build-with-id-test
  (is (= "C"
         (:build-name (#'build-matrix/get-build-with-id [(build "C" true)
                                                         (build "B" false)]
                                                        "C")))))

(defn- get-build-names [matrix]
  (walk/postwalk #(or (:build-name %)
                      %)
                 matrix))

(deftest build-matrix-filtering
  (is (= [["A" "B"]
          ["C" "D" "E"]]

         (get-build-names (build-matrix/place-builds-on-matrix
                           [["A" "B"]
                            ["C" "D" "E"]]
                           builds))))

  (testing "A build that cannot be found must still be rendered and not skipped"
    (is (= [["Mystery build"]]

           (get-build-names (build-matrix/place-builds-on-matrix
                             [["Mystery build"]]
                             builds))))))

(defcard-rg render-build-matrix
  (let [matrix [["C" "not found"]
                ["B" "D" "E"]]]
    [build-monitor-components/monitor
     (build-matrix/place-builds-on-matrix matrix
                                          builds)]))
