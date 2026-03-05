(ns gritum.core
  (:gen-class)
  (:require
   [gritum.web.core :as web]
   [gritum.db.migrate :as migrate]
   [gritum.config :as config]
   [taoensso.timbre :as log]))

(defn -main
  "The unified entry point for the gritum engine.
  Usage: clj -M:run [web|migrate]"
  [& args]
  (let [task (or (first args) "web")]
    (case task
      "web"     (web/-main)
      "migrate" (migrate/run)
      (do
        (log/error "Unknown task:" task)
        (println "Available tasks: web, migrate")
        (System/exit 1)))))
