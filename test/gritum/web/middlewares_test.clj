(ns gritum.web.middlewares-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [gritum.web.middlewares :as sut]
   [jsonista.core :as json]))

(deftest inject-headers-in-resp-test
  (testing "should add application/json Content-Type header to valid responses"
    (let [mock-handler (fn [_] {:status 200 :body "ok"})
          middleware (sut/inject-headers-in-resp mock-handler)
          response (middleware {})]
      (is (= "application/json; charset=utf-8"
             (get-in response [:headers "Content-Type"])))))

  (testing "should return nil if the handler returns nil"
    (let [mock-handler (fn [_] nil)
          middleware (sut/inject-headers-in-resp mock-handler)
          response (middleware {})]
      (is (nil? response)))))

(deftest turn-resp-body-to-bytes-test
  (testing "should convert Map body into JSON byte array"
    (let [data {:foo "bar"}
          mock-handler (fn [_] {:status 200 :body data})
          middleware (sut/turn-resp-body-to-bytes mock-handler)
          response (middleware {})]
      (is (bytes? (:body response)))
      (is (= data (json/read-value (:body response) json/keyword-keys-object-mapper)))))

  (testing "should return response as is if body is missing"
    (let [mock-handler (fn [_] {:status 204})
          middleware (sut/turn-resp-body-to-bytes mock-handler)
          response (middleware {})]
      (is (not (contains? response :body)))
      (is (= 204 (:status response))))))
