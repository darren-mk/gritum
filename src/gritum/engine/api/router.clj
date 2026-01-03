(ns gritum.engine.api.router
  (:require
   [reitit.ring :as ring]
   [gritum.engine.api.middlewares :as mw]
   [gritum.engine.core :as core]
   [gritum.engine.db.client :as db.client]
   [ring.middleware.multipart-params :as multp]
   [ring.middleware.params :as midp]
   [ring.util.http-response :as resp]))

(defn- handle-health [_]
  {:status 200
   :body {:status "up"
          :version core/version
          :timestamp (.toString (java.time.Instant/now))}})

(defn- evaluate-handler [req]
  (let [params (:multipart-params req)
        le-file (get params "le-file")
        cd-file (get params "cd-file")]
    (if (and le-file cd-file)
      (let [le-str (slurp (:tempfile le-file))
            cd-str (slurp (:tempfile cd-file))
            report (core/evaluate-xml le-str cd-str)]
        {:status 200 :body report})
      {:status 400
       :body {:error "Missing files"
              :details "le-file and cd-file are required"}})))

(defn login-handler [ds]
  (fn [{:keys [body] :as _req}]
    (let [{:keys [email password]} body
          client (db.client/authenticate ds email password)]
      (if client
        (-> (resp/ok {:message "Login successful"
                      :user (:clients/name client)})
            (assoc :session {:identity (:clients/id client)}))
        (resp/unauthorized {:error "Invalid email or password"})))))

(defn logout-handler [_req]
  (-> (resp/ok {:message "Logged out"})
      (assoc :session nil)))

(defn app [{:keys [ds]}]
  (let [auth-mw (mw/wrap-api-key-auth ds)]
    (ring/ring-handler
     (ring/router
      ["/api"
       ["/health" {:get handle-health}]
       ["/services"
        ["/v1" {:middleware [auth-mw]}
         ["/ping" {:get (fn [_] (resp/ok {:message "pong"}))}]
         ["/evaluate" {:post evaluate-handler}]]]
       ["/dashboard" {:middleware [mw/wrap-session]}
        ["/login" {:post (login-handler ds)}]
        ["/logout" {:post logout-handler}]
        ["/me" {:get (fn [req]
                       (if-let [id (get-in req [:session :identity])]
                         (resp/ok {:id id})
                         (resp/unauthorized)))}]]]
      {:data {:middleware [mw/wrap-exception
                           mw/wrap-api-cors
                           mw/inject-headers-in-resp
                           mw/read-body
                           mw/write-body
                           midp/wrap-params
                           multp/wrap-multipart-params]}}))))
