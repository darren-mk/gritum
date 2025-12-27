(ns user
  (:require
   [malli.dev :as mdev]
   [malli.dev.pretty :as pretty]))

(defn inst! []
  (mdev/start! {:report (pretty/reporter)}))

(defn unst! []
  (mdev/stop!))

(comment
  (inst!)
  (unst!))
