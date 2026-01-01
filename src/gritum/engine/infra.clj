(ns gritum.engine.infra)

(def Env
  [:enum :prod :local])

(def DbConfig
  [:map
   [:dbtype [:enum "postgresql"]]
   [:dbname :string]
   [:user [:enum "gritum_admin"]]
   [:password :string]
   [:host :string]
   [:port [:enum 5432]]])

(def MigrationConfig
  [:map
   [:store [:enum :database]]
   [:migration-dir [:enum "migrations"]]
   [:db DbConfig]])
