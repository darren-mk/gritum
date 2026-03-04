(ns gritum.config
  "Collection of function providing config data. 
  Functions that prepare and return a map are named with suffix `-cfg`."
  (:require [clojure.string :as str]))

(def Env
  [:enum :prod :local])

(defn- on! [k]
  (let [v (System/getenv k)]
    (if (str/blank? v)
      (let [msg (str "🚨 CRITICAL CONFIG ERROR: Environment variable '"
                     k "' is not set.")]
        (throw (ex-info msg {:variable k})))
      v)))

(defn env []
  (keyword (on! "GRITUM_ENV")))

(defn port []
  (Integer/parseInt (on! "PORT")))

(defn psql-cfg []
  (let [user (on! "DB_USER")
        password (on! "DB_PASSWORD")
        dbname (on! "DB_NAME")
        host (on! "DB_HOST")
        dbtype (on! "DB_TYPE")]
    (if (str/starts-with? host "/cloudsql/")
      (let [instance-name (str/replace host "/cloudsql/" "")]
        {:jdbcUrl (str "jdbc:postgresql:///" dbname
                       "?user=" user
                       "&password=" password
                       "&socketFactory=" "com.google.cloud.sql.postgres.SocketFactory"
                       "&cloudSqlInstance=" instance-name)})
      {:dbtype dbtype
       :dbname dbname
       :host host
       :port (Integer/parseInt (on! "DB_PORT"))
       :user user
       :password password})))

(defn psql-mig-cfg []
  {:store :database
   :migration-dir "migrations"
   :db (psql-cfg)})

(defn llm-cfg []
  {:ai-api-key (on! "LLM_API_KEY")
   :ai-model (on! "LLM_MODEL")})
