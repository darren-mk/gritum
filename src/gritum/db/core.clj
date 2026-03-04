(ns gritum.db.core
  (:require
   [integrant.core :as ig]
   [next.jdbc.connection :as connection])
  (:import
   (com.zaxxer.hikari HikariDataSource)))

(defmethod ig/init-key :db/sql [_ db-cfg]
  (println "📡 Initializing HikariCP connection pool...")
  (connection/->pool HikariDataSource db-cfg))

(defmethod ig/halt-key! :db/sql [_ datasource]
  (println "🛑 Closing HikariCP connection pool...")
  (when (instance? java.io.Closeable datasource)
    (.close datasource)))
