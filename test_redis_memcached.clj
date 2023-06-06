(ns test-redis-memcached
  (:require [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [jsonista.core :as j]))

(def redis-api-base "https://v9bk7aih1b.execute-api.us-east-1.amazonaws.com/prod")

(def memcached-api-base "https://wstl7ypxnh.execute-api.us-east-1.amazonaws.com/prod/")

(defn run-times [n fn]
  (if (= n 0)
    ()
    (let [result (fn)]
      (conj (run-times (dec n) fn) result))))

(defn reset [url]
  (let [response (client/post url)
        res (Integer/parseInt (:body response))]
    (println res)
    res))

(defn reset-redis []
  (reset (str redis-api-base "/reset-redis")))

(defn reset-memcached []
  (reset (str memcached-api-base "/reset-memcached")))

(defn charge-request [url service-type unit]
  (let [body (j/write-value-as-string
               {:serviceType service-type
                :unit unit})]
    (let [response (client/post url {:body body})
          response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
      (println body " -> " response-body)
      response-body)))

(defn charge-request-redis [service-type unit]
  (charge-request (str redis-api-base "/charge-request-redis")
    service-type unit))

(defn charge-request-memcached [service-type unit]
  (charge-request (str memcached-api-base "/charge-request-memcached")
    service-type unit))

(defn test-system [title reset-fn charge-request-fn]
  (println "---------------- TEST " title " ------------------")
  (assert (= (reset-fn) 100))
  (let [remaining-balances (run-times 21 #(charge-request-fn "voice" 2))]
    (assert (= remaining-balances
              '({:charges 5, :remainingBalance 95, :isAuthorized true}
                {:charges 5, :remainingBalance 90, :isAuthorized true}
                {:charges 5, :remainingBalance 85, :isAuthorized true}
                {:charges 5, :remainingBalance 80, :isAuthorized true}
                {:charges 5, :remainingBalance 75, :isAuthorized true}
                {:charges 5, :remainingBalance 70, :isAuthorized true}
                {:charges 5, :remainingBalance 65, :isAuthorized true}
                {:charges 5, :remainingBalance 60, :isAuthorized true}
                {:charges 5, :remainingBalance 55, :isAuthorized true}
                {:charges 5, :remainingBalance 50, :isAuthorized true}
                {:charges 5, :remainingBalance 45, :isAuthorized true}
                {:charges 5, :remainingBalance 40, :isAuthorized true}
                {:charges 5, :remainingBalance 35, :isAuthorized true}
                {:charges 5, :remainingBalance 30, :isAuthorized true}
                {:charges 5, :remainingBalance 25, :isAuthorized true}
                {:charges 5, :remainingBalance 20, :isAuthorized true}
                {:charges 5, :remainingBalance 15, :isAuthorized true}
                {:charges 5, :remainingBalance 10, :isAuthorized true}
                {:charges 5, :remainingBalance 5, :isAuthorized true}
                {:charges 5, :remainingBalance 0, :isAuthorized true}
                {:charges 0, :remainingBalance 0, :isAuthorized false})))
    (println "ASSERTIONS PASS")))

(test-system "redis" reset-redis charge-request-redis)
(test-system "memcached" reset-memcached charge-request-memcached)
