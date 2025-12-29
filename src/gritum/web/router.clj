(ns gritum.web.router
  (:require
   [reitit.ring :as ring]
   [reitit.ring.middleware.multipart :as multipart]
   [gritum.web.views :as views]
   [gritum.evaluate :as eval]
   [clojure.data.xml :as xml]))

(defn- handle-evaluate [req]
  (let [params (:multipart-params req)
        ;; Process uploaded files
        le-xml (xml/parse-str (slurp (get-in params ["le-file" :tempfile])))
        cd-xml (xml/parse-str (slurp (get-in params ["cd-file" :tempfile])))
        ;; Core logic call
        report (eval/perform le-xml cd-xml)]
    {:status 200
     :body (views/evaluation-result report)}))

(def app
  (ring/ring-handler
   (ring/router
    [""
     ["/" {:get (fn [_] {:status 200 :body (views/home-page)})}]
     ["/evaluate" {:post handle-evaluate}]
     {:data {:middleware [multipart/create-multipart-middleware]}}])))
