(ns gritum.system
  (:require
   [integrant.core :as ig]
   [org.httpkit.server :as http]
   [gritum.web.router :as router]
   [gritum.config :as config]
   [next.jdbc.connection :as connection]
   [taoensso.timbre :as log])
  (:import
   (com.zaxxer.hikari HikariDataSource)))

;; --- Configuration ---

(defn system-config []
  {:db/sql (config/psql-cfg)
   :web/handler {:db/sql (ig/ref :db/sql)}
   :web/server {:web/port (config/port)
                :web/handler (ig/ref :web/handler)}})

;; --- Database Component ---

(defmethod ig/init-key :db/sql [_ psql-cfg]
  (log/info "📡 Initializing HikariCP connection pool...")
  (connection/->pool HikariDataSource psql-cfg))

(defmethod ig/halt-key! :db/sql [_ datasource]
  (log/info "🛑 Closing HikariCP connection pool...")
  (when (instance? java.io.Closeable datasource)
    (.close datasource)))

;; --- Web Handler Component ---

(defmethod ig/init-key :web/handler [_ {db-sql :db/sql}]
  (log/info "🕸️ Initializing Web Router...")
  (router/app db-sql))

;; --- Web Server Component ---

(defmethod ig/init-key :web/server [_ {:keys [web/handler web/port]}]
  (log/info "🚀 Starting HTTP server on port:" port)
  (http/run-server handler {:port port}))

(defmethod ig/halt-key! :web/server [_ stop-fn]
  (log/info "🔌 Stopping HTTP server...")
  (stop-fn :timeout 100))
