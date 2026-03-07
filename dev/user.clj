(ns user
  (:require
   [integrant.repl :as ir]
   [gritum.system :as system]
   [gritum.db.migrate :as mig]
   [malli.dev :as mdev]
   [malli.dev.pretty :as mpret]))

(ir/set-prep!
 system/system-config)

(defn inst []
  (mdev/start!
   {:report (mpret/reporter)}))

(defn unst []
  (mdev/stop!))

(defn start []
  (inst)
  (ir/go))

(defn stop []
  (ir/halt))

(defn restart []
  (ir/reset))

(defn create-migration [s]
  (mig/create s))

(defn run-migrations []
  (mig/run))

(comment
  (inst)
  (unst)
  (start)
  (stop)
  (restart)
  (create-migration "some-name")
  (run-migrations))
