(ns gritum.core
  (:gen-class)
  (:require
   [integrant.core :as ig]
   [gritum.system :as system]
   [gritum.db.migrate :as migrate]
   [gritum.config :as config]
   [taoensso.timbre :as log]))

(defn -main
  "The unified entry point for the gritum engine.
  Usage: clj -M:run [web|migrate]"
  [& args]
  (let [task (first args)]
    (case task
      "web" (let [system (ig/init (system/system-config))]
              (.addShutdownHook
               (Runtime/getRuntime)
               (Thread. #(ig/halt! system)))
              (log/info "gritum engine is running on port" (config/port)))
      "migrate" (migrate/run)
      (do
        (log/error "Unknown task:" task)
        (println "Available tasks: web, migrate")
        (System/exit 1)))))
