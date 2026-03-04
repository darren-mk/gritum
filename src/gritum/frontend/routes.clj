(ns gritum.frontend.routes
  (:require
   [gritum.frontend.pages.dashboard :as pg.dashboard]
   [gritum.frontend.pages.docs :as pg.docs]
   [gritum.frontend.pages.home :as pg.home]
   [gritum.frontend.pages.login :as pg.login]
   [gritum.frontend.pages.pricing :as pg.pricing]
   [gritum.frontend.pages.signup :as pg.signup]
   [gritum.frontend.pages.lab :as pg.lab]
   [gritum.frontend.handlers :as handlers]))

(defn pages [mws]
  ["" {:middleware mws}
   ["/" {:get pg.home/handler}]
   ["/docs" {:get pg.docs/handler}]
   ["/login" {:get pg.login/handler}]
   ["/pricing" {:get pg.pricing/handler}]
   ["/signup" {:get pg.signup/handler}]
   ["/dashboard" {:get pg.dashboard/handler}]
   ["/lab" {:get pg.lab/handler}]])

(defn hypermedia [mws ds]
  ["/hypermedia" {:middleware mws}
   ["/hello" {:get handlers/hello}]
   ["/login" {:post (handlers/login ds)}]
   ["/logout" {:post handlers/logout}]
   ["/signup" {:post (handlers/signup ds)}]])
