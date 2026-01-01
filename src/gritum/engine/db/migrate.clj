(ns gritum.engine.db.migrate
  (:require
   [clojure.edn :as edn]
   [clojure.string :as cstr]
   [clojure.java.io :as io]
   [gritum.engine.infra :as inf]
   [migratus.core :as migratus]))

(defn get-config
  {:malli/schema [:=> [:cat inf/Env] inf/MigrationConfig]}
  [env]
  (let [config (-> "config.edn" io/file
                   slurp edn/read-string)
        {:keys [db migration]} (get config env)]
    (if db {:store :database
            :migration-dir (:dir migration)
            :db db}
        (throw (ex-info "no config found" {:env env})))))

(defn create
  "populates up migration file only as
  we follow forward-only principle"
  {:malli/schema [:=> [:cat inf/Env :string] :any]}
  [env name]
  (migratus/create (get-config env) name))

(defn run
  {:malli/schema [:=> [:cat inf/Env] :any]}
  [env]
  (let [config (get-config env)]
    (println (str "🚀 running migrations for [" env "]..."))
    (migratus/migrate config)))

(defn -main [& args]
  (let [env (keyword (or (first args) "local"))]
    (run env)))
