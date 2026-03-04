(ns gritum.db.migrate
  (:require
   [gritum.config :as config]
   [migratus.core :as migratus]))

(defn create
  "populates up migration file only as
  we follow forward-only principle"
  {:malli/schema [:=> [:cat :string] :any]}
  [name]
  (migratus/create (config/psql-mig-cfg) name))

(defn run []
    (println "🚀 running migrations")
    (migratus/migrate (config/psql-mig-cfg))
    (println "✅ migrations completed successfully."))

(defn -main [& _args]
  (run))
