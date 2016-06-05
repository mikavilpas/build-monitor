(ns build-monitor.test.devcards
  (:require [build-monitor.build-matrix-tests :as build-matrix-tests]
            [build-monitor.config-test :as config-test]
            [build-monitor.components.recent-events-components-tests :as recent-events-components-tests]
            [build-monitor.time-processing-tests :as time-processing-tests]
            [build-monitor.special-effects.spinner-tests :as spinner-tests]
            [build-monitor.special-effects.heart-tests :as heart-tests]
            [build-monitor.pubsub-tests :as pubsub-tests]
            [build-monitor.test.components.monitor-components-tests :as monitor-components-tests]
            [build-monitor.components.config-components-tests :as config-components-tests]
            [build-monitor.test.utils-tests :as utils-tests]
            [build-monitor.test.app.app-environment-tests :as app-environment-tests]
            [build-monitor.special-effects.backend-stuck-indicator-tests :as backend-stuck-indicator-tests]
            [build-monitor.mock-data.api-build-creator-tests :as api-build-creator-tests]
            [build-monitor.storage-tests :as storage-tests]
            [schema.core :as s]
            [build-monitor.data-source.travis-client-tests :as travis-client-tests]))

(s/set-fn-validation! false)

;; todo more elegant way of including other devcards files
monitor-components-tests/include-me
build-matrix-tests/include-me
config-test/include-me
utils-tests/include-me
spinner-tests/include-me
pubsub-tests/include-me
time-processing-tests/include-me
heart-tests/include-me
storage-tests/include-me
config-components-tests/include-me
app-environment-tests/include-me
backend-stuck-indicator-tests/include-me
recent-events-components-tests/include-me
api-build-creator-tests/include-me
travis-client-tests/include-me
