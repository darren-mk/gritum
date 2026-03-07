(ns gritum.helper
  (:require
   [malli.dev :as mdev]
   [malli.dev.pretty :as mpret]))

(defn inst []
  (mdev/start!
   {:report (mpret/reporter)}))
