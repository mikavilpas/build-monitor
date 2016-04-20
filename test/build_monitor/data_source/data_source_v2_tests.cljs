(ns build-monitor.data-source.data-source-v2-tests
  (:require [build-monitor.data-source.common :as common]
            [build-monitor.data-source.data-source-v2 :as data]
            [build-monitor.test-utils :as test-utils]
            [build-monitor.types :as types]
            [cljs.core.async :as async :refer [<!]]
            [cljs.test :refer-macros [async is testing]]
            [schema.core :as s :include-macros true])
  (:require-macros
   [cljs.core.async.macros :refer [go]]
   [devcards.core :as dc :refer [defcard deftest defcard-rg]]))

(def include-me :yep)

(def real-api-url "https://localhost/BuildMonitorStaging/api/Builds/GetBuildsWithUpdateTime/")
(def data-source (let [common-errors []]
                   (data/DataSourceForVersion2. real-api-url common-errors)))

(deftest api-returns-expected-data
  (async test-done
         (go (let [api-build (first (<! (s/with-fn-validation
                                          (common/get-build-statuses data-source))))]
               (is (not-empty
                    "if this is displayed, the test has validated that the builds coming"
                    "from the api satisfy their schemas - there is a bug with devcards and"
                    "validation so you see this message."))
               (test-done)))))

(deftest get-short-build-number-test
  (is (= "357"
         (data/get-short-build-number "Some Build.357"))))

(deftest identifies-common-errors
  (is (= test-utils/validate-merges
         (s/with-fn-validation
           (data/get-first-common-build-error ["Yep, this build is broken because ValidateMerges. Some text after"]
                                              test-utils/all-common-errors))))

  (testing "can detect a build error before detecting a ValidateMerges error"
    (is (= test-utils/compilation-error
           (s/with-fn-validation
             (data/get-first-common-build-error
              ["Overall Build Process"
               "Overall build process"
               "Update build number"
               "Run on agent"
               "Initialize environment"
               "Get sources from Team Foundation Version Control"
               "Not Labeling sources"
               "Run build script"
               "The process cannot access the file 'C:\\WP1\\146\\src\\foo' because it is being used by another process."
               "Building solution 'C:\\WP1\\146\\src\\"
               "foo.sln' failed, see output for further details!"
               "+ throw \"Building solution '$solution' failed, see output for"
               "further ..."]
              test-utils/all-common-errors)))))

  (testing "can detect an error where a file is not accessible"
    (is (= test-utils/unauthorized-access-error
           (data/get-first-common-build-error
            ["Overall Build Process"
             "Overall build process"
             "Update build number"
             "Run on agent"
             "Initialize environment"
             "Get sources from Team Foundation Version Control"
             "Exception Message: Access to the path 'foo' is denied. (type UnauthorizedAccessException) Exception Stack Trace: at System.IO.__Error.WinIOError"]
            [test-utils/unauthorized-access-error])))))

(defcard parse-build-data
  (test-utils/channel->atom
   (go (let [response (<! (common/get-build-statuses data-source))]
         (take 3 (:builds response))))))

(defcard view-build-data
  (test-utils/channel->atom
   (common/get-body real-api-url)))

(defcard-rg parse-test-json
  (fn [state _]
    [:pre (test-utils/pprint
           (map #(data/api-build->BuildStatus % []) (:Builds @state)))])
  (test-utils/channel->atom
   (common/get-body "/test.json"))
  {:inspect-data true})

(deftest latest-successful-build-is-parsed-correctly
  (let [build {:LatestSuccessfulBuild {:StartTime "2016-01-27T10:37:45.547Z"
                                       :FinishTime "2016-01-27T10:40:05.64Z"}
               :StartTime nil
               :FinishTime nil}
        common-build-errors []]
    (is (not (nil? (-> (data/api-build->BuildStatus build common-build-errors)
                       :latest-successful-build
                       :start-time))))))
