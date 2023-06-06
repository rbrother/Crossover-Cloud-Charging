(ns test-redis-memcached
  (:require [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [jsonista.core :as j]))

(def redis-api-base "https://v9bk7aih1b.execute-api.us-east-1.amazonaws.com/prod")

(def memcached-api-base "https://wstl7ypxnh.execute-api.us-east-1.amazonaws.com/prod/")

(defn run-times [n fn]
  (fn)
  (if (> n 0)
    (run-times (dec n) fn)))

(defn reset [url]
  (let [response (client/post url)
        res (Integer/parseInt (:body response))]
    (println res)
    res))

(defn reset-redis []
  (println "------------ reset redis --------------")
  (reset (str redis-api-base "/reset-redis")))

(defn reset-memcached []
  (println "------------ reset memcached --------------")
  (reset (str memcached-api-base "/reset-memcached")))

(defn charge-request [url service-type unit ]
  (let [body (j/write-value-as-string
               {:serviceType service-type
                :unit unit})]
    (println body)
    (let [response (client/post url {:body body})
          response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
      (pprint response-body)
      response-body)))

(defn charge-request-redis [service-type unit]
  (println "------------ charge-request-redis --------------")
  (charge-request (str redis-api-base "/charge-request-redis")
    service-type unit))

(defn charge-request-memcached [service-type unit]
  (println "------------ charge-request-memcached --------------")
  (charge-request (str memcached-api-base "/charge-request-memcached")
    service-type unit))

(defn test-redis []
  (assert (= (reset-redis) 100))
  (run-times 5 #(charge-request-redis "voice" 2)))

(defn test-memcached []
  (assert (= (reset-memcached) 100))
  (run-times 5 #(charge-request-memcached "voice" 2)))

(test-redis)
(test-memcached)