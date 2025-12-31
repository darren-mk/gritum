(ns gritum.engine.auth
  (:require
   [taoensso.timbre :as log]))

(defn create-auth-service [_db]
  (log/info "initializing auth service with db connection")
  (fn [api-key]
    ;; This inner function is executed for every request.
    ;; Currently a mock implementation, but can be
    ;; seamlessly replaced with (jdbc/execute! db [...]) later.
    (let [mock-db #{"gritum-api-test-key"}]
      (contains? mock-db api-key))))
