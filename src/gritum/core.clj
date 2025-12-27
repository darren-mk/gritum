(ns gritum.core)

#_#_#_
(defmulti normalize
  (fn [_ k] k))

(defmethod normalize :fee [xml _]
  (let [detail (traverse xml [:FEE :FEE_DETAIL])
        fee-content (:content xml)
        payments-node (first (filter #(= :FEE_PAYMENTS (:tag %)) fee-content))
        payments (when payments-node
                   (filter #(= :FEE_PAYMENT (:tag %)) (:content payments-node)))
        section (->> [:IntegratedDisclosureSectionType] (traverse detail) first)
        fee-type (->> [:EXTENSION :OTHER :FEE_DETAIL_EXTENSION :FeeItemType]
                      (traverse detail) first)
        in-apr? (contains? apr-mandatory-fees fee-type)]
    #:fee{:type fee-type :section section
          :tolerance (->tolerance section fee-type)
          :amount (if (seq payments)
                    (->> payments
                         (filter #(= "Buyer" (first (traverse % [:FEE_PAYMENT :FeePaymentPaidByType]))))
                         (map #(->> [:FEE_PAYMENT :FeeActualPaymentAmount] (traverse %) first parse-double))
                         (reduce + 0.0))
                    0.0)
          :in-apr? in-apr?
          
          :paid-by "Buyer"}))

(defn ->domain [xml]
  (let [loan (traverse xml loan-path)
        fees (traverse loan [:FEE_INFORMATION :FEES])]
    {:fees (mapv #(normalize % :fee) fees)}))

