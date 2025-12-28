(ns gritum.evaluate-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [gritum.evaluate :as sut]))

(defn- wrap-in-mismo
  "Wraps fee nodes into the required MISMO XML hierarchy."
  [fees]
  {:tag :MESSAGE
   :content
   [{:tag :DOCUMENT
     :content
     [{:tag :DEAL_SETS
       :content
       [{:tag :DEAL
         :content
         [{:tag :LOANS
           :content
           [{:tag :LOAN
             :content
             [{:tag :FEE_INFORMATION
               :content
               [{:tag :FEES
                 :content fees}]}]}]}]}]}]}]})

(defn- mock-fee [section category amount _id]
  {:tag :FEE
   :content [{:tag :FEE_DETAIL
              :content [{:tag :IntegratedDisclosureSectionType :content [(name section)]}
                        {:tag :EXTENSION
                         :content [{:tag :OTHER
                                    :content [{:tag :ucd:FEE_DETAIL_EXTENSION
                                               :content [{:tag :ucd:FeeItemType 
                                                          :attrs {:DisplayLabelText "Test"}
                                                          :content [(name category)]}]}]}]}]}
             {:tag :FEE_PAYMENTS
              :content [{:tag :FEE_PAYMENT
                         :content [{:tag :FeeActualPaymentAmount :content [(str amount)]}
                                   {:tag :FeePaymentPaidByType :content ["Buyer"]}]}]}]})

(deftest evaluate-perform-test
  (testing "Combined evaluation of 0% and 10% tolerance rules"
    (let [;; LE: Total 0% = 500, Total 10% = 1000
          le-xml (wrap-in-mismo
                   [(mock-fee :origination-charges :admin-fee 500.0 "a")
                    (mock-fee :services-shop :title-search 1000.0 "b")])
          ;; CD: 0% increases by 50, 10% increases by 150 (Total 1150)
          ;; 10% Rule: 1000 * 1.1 = 1100 threshold. 1150 - 1100 = 50 cure.
          ;; Total Cure should be 50 (Zero) + 50 (Ten) = 100.
          cd-xml (wrap-in-mismo
                   [(mock-fee :origination-charges :admin-fee 550.0 "a")
                    (mock-fee :services-shop :title-search 1150.0 "b")])
          result (sut/perform le-xml cd-xml)]
      (is (= 100.0 (:total-cure result)) "Total cure should sum both 0% and 10% violations")
      (is (not (:valid? result)) "Should be invalid due to cure amount")
      (is (contains? (:breakdown result) :zero-percent))
      (is (contains? (:breakdown result) :ten-percent)))))

(deftest missing-fees-path-test
  (testing "Robustness when the FEES path is missing in XML"
    (let [empty-xml {:tag :MESSAGE :content []}
          result (sut/perform empty-xml empty-xml)]
      (is (= 0.0 (:total-cure result)))
      (is (:valid? result))
      (is (empty? (get-in result [:breakdown :zero-percent :violations]))))))
