(ns gritum.test-helper
  (:require
   [clojure.edn :as edn]
   [clojure.data.xml :as xml]
   [clojure.java.io :as io]))

(defn load-edn [filename]
  (if-let [resource-url (io/resource filename)]
    (-> resource-url slurp edn/read-string)
    (throw (Exception. (str "Resource not found: " filename)))))

(defn load-xml [filename]
  (if-let [resource-url (io/resource filename)]
    (-> resource-url slurp xml/parse-str)
    (throw (Exception. (str "Resource not found: " filename)))))

(defn complete-input-for-fees
  [fees-content fee-summary-detail-content]
  {:tag :MESSAGE
   :attrs {:MISMOReferenceModelIdentifier "3.3.0299"}
   :content
   [{:tag :DOCUMENT_SETS :attrs {}
     :content
     [{:tag :DOCUMENT_SET :attrs {}
       :content
       [{:tag :DOCUMENTS :attrs {}
         :content
         [{:tag :DOCUMENT :attrs {}
           :content
           [{:tag :DEAL_SETS :attrs {}
             :content
             [{:tag :DEAL_SET :attrs {}
               :content
               [{:tag :DEALS :attrs {}
                 :content
                 [{:tag :DEAL :attrs {}
                   :content
                   [{:tag :LOANS :attrs {}
                     :content
                     [{:tag :LOAN :attrs {}
                       :content
                       [{:tag :FEE_INFORMATION :attrs {}
                         :content
                         [{:tag :FEES :attrs {}
                           :content fees-content}
                          {:tag :FEES_SUMMARY :attrs {}
                           :content
                           [{:tag :FEE_SUMMARY_DETAIL :attrs {}
                             :content fee-summary-detail-content}]}]}]}]}]}]}]}]}]}]}]}]}]})

