(ns gritum.engine.configs
  (:require [clojure.string :as str]))

(def Env
  [:enum :prod :local])

(defn- bring! [k]
  (let [v (System/getenv k)]
    (if (str/blank? v)
      (let [msg (str "🚨 CRITICAL CONFIG ERROR: Environment variable '"
                     k "' is not set.")]
        (throw (ex-info msg {:variable k})))
      v)))

(defn get-env []
  (keyword (bring! "GRITUM_ENV")))

(defn get-port []
  (Integer/parseInt (bring! "PORT")))

(defn get-db-config []
  (let [user (bring! "DB_USER")
        password (bring! "DB_PASSWORD")
        dbname (bring! "DB_NAME")
        host (bring! "DB_HOST")
        dbtype (bring! "DB_TYPE")]
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
       :port (Integer/parseInt (bring! "DB_PORT"))
       :user user
       :password password})))

(defn get-llm-config []
  {:ai-api-key (bring! "LLM_API_KEY")
   :ai-model (bring! "LLM_MODEL")})
