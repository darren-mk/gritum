(ns gritum.web.middlewares
  (:require
   [jsonista.core :as json]))

(defn inject-headers-in-resp [handler]
  (let [m {"Content-Type"
           "application/json; charset=utf-8"}]
    (fn [req]
      (let [resp (handler req)]
        (if resp
          (update resp :headers merge m)
          resp)))))

(defn turn-resp-body-to-bytes [handler]
  (let [f #(json/write-value-as-bytes
            % json/default-object-mapper)]
    (fn [req]
      (let [{:keys [body] :as resp} (handler req)]
        (if body
          (update resp :body f)
          resp)))))
