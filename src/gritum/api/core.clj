(ns gritum.api.core
  (:gen-class)
  (:require
   [integrant.core :as ig]
   [org.httpkit.server :as http]
   [gritum.api.router :as router]
   [gritum.db.core]
   [gritum.config :as config]
   [taoensso.timbre :as log]))

(defn system-config []
  {:db/sql (config/psql-cfg)
   :web/handler {:db/sql (ig/ref :db/sql)}
   :web/server {:web/port (config/port)
                :web/handler (ig/ref :web/handler)}})

(defmethod ig/init-key :web/handler
  [_ {db-sql :db/sql}]
  (log/info "Initializing Web Router...")
  (router/app db-sql))

(defmethod ig/init-key :web/server
  [_ {:keys [web/handler web/port]}]
  (log/info "Starting HTTP server on port:" port)
  (http/run-server handler {:port port}))

(defmethod ig/halt-key! :web/server
  [_ stop-fn]
  (log/info "Stopping HTTP server...")
  (stop-fn :timeout 100))

(defn -main
  [& _args]
  (let [system (ig/init (system-config))]
    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread. #(ig/halt! system)))
    (log/info "gritum engine is running on port "
              (config/port))))
