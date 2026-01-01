(ns user
  (:require
   [integrant.repl :as ir]
   [gritum.engine.api.core :as w]
   [gritum.engine.db.migrate :as mig]
   [malli.dev :as mdev]
   [malli.dev.pretty :as pretty]))

(ir/set-prep! (fn [] w/config))

(defn inst []
  (mdev/start!
   {:report (pretty/reporter)}))

(defn unst []
  (mdev/stop!))

(defn go []
  (inst)
  (ir/go))

(defn halt []
  (ir/halt))

(defn rego []
  (ir/reset))

(defn create-mig [s]
  (mig/create :local s))

(defn run-mig []
  (mig/run :local))

(comment
  (inst)
  (unst)
  (go)
  (halt)
  (rego)
  (create-mig "some-name")
  (run-mig))
