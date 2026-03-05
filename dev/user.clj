(ns user
  (:require
   [integrant.repl :as ir]
   [gritum.web.core :as w]
   [gritum.db.migrate :as mig]
   [malli.dev :as mdev]
   [malli.dev.pretty :as pretty]))

(ir/set-prep! (fn [] (w/system-config)))

(defn in []
  (mdev/start!
   {:report (pretty/reporter)}))

(defn un []
  (mdev/stop!))

(defn go []
  (in)
  (ir/go))

(defn no []
  (ir/halt))

(defn re []
  (ir/reset))

(defn cm [s]
  (mig/create s))

(defn rm []
  (mig/run))

(comment
  (in)
  (un)
  (go)
  (no)
  (re)
  (cm "some-name")
  (rm))
