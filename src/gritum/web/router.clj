(ns gritum.web.router
  (:require
   [gritum.web.middleware :as mw]
   [gritum.config :as config]
   [gritum.db.api-key :as db.api-key]
   [gritum.frontend.routes :as route.web]
   [reitit.coercion.malli :as rcmal]
   [reitit.ring :as ring]
   [reitit.openapi :as openapi]
   [ring.middleware.multipart-params :as multp]
   [ring.middleware.params :as midp]
   [ring.util.http-response :as resp]
   [gritum.domain.model :as dom]))

(defn- handle-health [_]
  {:status 200
   :body {:status "up"
          :version config/version
          :timestamp (.toString (java.time.Instant/now))}})

(defn logout-handler [_req]
  (-> (resp/ok {:message "Logged out"})
      (assoc :session nil)))

(defn pong-handler [_]
  (resp/ok {:message "pong"}))

(defn me-handler [req]
  (resp/ok {:id (get-in req [:session :identity])}))

(defn create-api-key-handler [ds]
  (fn [req]
    (let [client-id (get-in req [:session :identity])
          raw-key (db.api-key/create! ds client-id)
          msg "API key created successfully."]
      (resp/ok {:api_key raw-key
                :message msg}))))

(defn list-api-keys-handler [ds]
  (fn [req]
    (let [client-id (get-in req [:session :identity])
          api-keys (db.api-key/list-by-client ds client-id)]
      (resp/ok api-keys))))

(defn app [db-sql]
  (let [auth-mw (mw/wrap-api-key-auth db-sql)]
    (ring/ring-handler
     (ring/router
      [(route.web/pages [mw/wrap-session
                         mw/content-type-html
                         mw/wrap-hiccup])
       (route.web/hypermedia [mw/wrap-session
                              mw/read-body] db-sql)
       ["/openapi.json"
        {:get {:no-doc true
               :handler (openapi/create-openapi-handler)}}]
       ["/api" {:coercion rcmal/coercion
                :middleware [mw/write-body-as-bytes]}
        ["/health" {:get handle-health}]
        ["/services" {:middleware [mw/content-type-json
                                   auth-mw
                                   mw/wrap-public-cors
                                   mw/read-body]}
         ["/v1"
          ["/ping" {:get {:responses {200 {:body [:map [:message :string]]}}
                          :handler pong-handler}}]]]
        ["/auth" {:middleware [mw/wrap-require-auth]}
         ["/api-keys" {:get {:summary "returns api keys for the client"
                             :responses {200 {:body [:sequential dom/ApiKey]}}
                             :handler (list-api-keys-handler db-sql)}
                       :post {:summary "create a api key for the client"
                              :response {200 {:body [:map [:message :string [:api_key :string]]]}}
                              :handler (create-api-key-handler db-sql)}}]
         ["/me" {:get me-handler}]
         ["/logout" {:post logout-handler}]]]]
      {:data {:middleware [mw/wrap-exception
                           midp/wrap-params
                           multp/wrap-multipart-params]}}))))
