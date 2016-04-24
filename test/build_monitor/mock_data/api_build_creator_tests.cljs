(ns build-monitor.mock-data.api-build-creator-tests
  (:require [build-monitor.mock-data.api-build-creator :as api-build-creator]
            [build-monitor.test.app.app-environment-tests :as app-environment-tests]
            [build-monitor.build-status-builder :as status-builder]
            [cljs.test :refer-macros [is testing]])
  (:require-macros [devcards.core :as dc :refer [defcard-rg deftest defcard]]))

(def include-me :yep)

(def build (->> (status-builder/build "The best build" true)
                (status-builder/commit "Fix everything, also optimize" "Antti Alpha")
                (status-builder/branch-information-uri "#branch-info")))

(defcard-rg converted-build
  (api-build-creator/build-status->api-build build))

(defcard-rg serialized-builds-as-json
  [:pre
   (api-build-creator/prettify
    (mapv api-build-creator/serialize-build-status
          app-environment-tests/builds))])
