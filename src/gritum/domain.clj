(ns gritum.domain
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.string :as cstr]
   [malli.util :as mu]
   [gritum.extract :as ext]))

(def Xml
  [:map
   [:tag :keyword]
   [:content :any]])

(def StakeholderKind
  [:enum :buyer :seller :lender])

(def Payee
  [:map
   [:name :string]
   [:kind StakeholderKind]])

(def Section
  [:enum
   :origination-charges
   :services-not-shop
   :services-shop
   :taxes :prepaids
   :initial-escrow
   :other-costs])

(def Timing
  [:enum :at-closing :before-closing])

(def Payment
  [:map
   [:amount [:and :double [:>= 0]]]
   [:payer StakeholderKind]
   [:timing Timing]
   [:meta {:optional true} :map]])

(def Fee
  [:map
   [:id :string]
   [:section Section]
   [:category :keyword]
   [:label :string]
   [:payee Payee]
   [:payments [:vector Payment]]
   [:meta {:optional true} :map]])

(defn ->payment
  {:malli/schema [:=> [:cat Xml] Payment]}
  [payment-xml]
  (let [payer (or (some->> [:FEE_PAYMENT :FeePaymentPaidByType]
                           (ext/traverse payment-xml) first
                           cstr/lower-case keyword)
                  :buyer)
        poc-ind (some->> [:FEE_PAYMENT :FeePaymentPaidOutsideOfClosingIndicator]
                         (ext/traverse payment-xml) first)
        timing (case poc-ind "true" :before-closing "false" :at-closing :at-closing)
        amount (some->> [:FEE_PAYMENT :FeeActualPaymentAmount]
                        (ext/traverse payment-xml) first parse-double)]
    {:amount amount :timing timing :payer payer}))

(defn ->fee
  {:malli/schema [:=> [:cat Xml] Fee]}
  [fee-xml]
  (let [detail (ext/traverse fee-xml [:FEE :FEE_DETAIL])
        payments (mapv ->payment (ext/traverse fee-xml [:FEE :FEE_PAYMENTS]))
        payee-name (or (some->> [:EXTENSION :OTHER :ucd:FEE_DETAIL_EXTENSION
                                 :ucd:FeePaidToEntityName] (ext/traverse detail) first)
                       "Unknown Payee")
        payee-kind-str (->> [:FeePaidToType] (ext/traverse detail) first)
        payee-kind (case payee-kind-str "Lender" :lender "ThirdParty" :seller :lender)
        section-str (->> [:IntegratedDisclosureSectionType] (ext/traverse detail) first)
        section (or (some-> section-str csk/->kebab-case-keyword) :unknown-section)
        fee-item-type-node (->> [:EXTENSION :OTHER :ucd:FEE_DETAIL_EXTENSION]
                                (ext/traverse detail) first)
        category-str (-> fee-item-type-node :content first)
        category (or (some-> category-str csk/->kebab-case-keyword) :unknown-category)
        label (or (-> fee-item-type-node :attrs :DisplayLabelText) category-str)
        id (str (name section) "_" (name category))]
    {:id id
     :section section
     :category category
     :label label
     :payee {:name payee-name
             :kind payee-kind}
     :payments payments}))
