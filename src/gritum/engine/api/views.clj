(ns gritum.engine.api.views
  (:require
   [hiccup.page :refer [html5 include-js include-css]]))

(defn layout [title content]
  (html5
   [:head
    [:title title]
    (include-js "https://cdn.jsdelivr.net/gh/starfederation/datastar@1.0.0-RC.7/bundles/datastar.js")
    (include-css "https://cdn.jsdelivr.net/npm/water.css@2/out/water.css")]
   [:body
    [:header [:h1 "Gritum Tolerance Engine"]]
    [:main content]]))

(defn home-page []
  (layout "Dashboard"
          [:div
           [:h2 "Upload XML Files for Evaluation"]
           [:form {:id "upload-form"
                   :hx-post "/evaluate"
                   :hx-target "#result"
                   :hx-encoding "multipart/form-data"}
            [:label "Initial Loan Estimate (LE)"]
            [:input {:type "file" :name "le-file" :accept ".xml"}]
            [:label "Final Closing Disclosure (CD)"]
            [:input {:type "file" :name "cd-file" :accept ".xml"}]
            [:button {:type "submit"} "Run Evaluation"]]
           [:div {:id "result" :style "margin-top: 2rem;"}
            "Results will appear here..."]]))

(defn evaluation-result [report]
  [:div
   [:h3 "Evaluation Result"]
   [:p "Total Cure Required: " [:strong (str "$" (:total-cure report))]]
   [:table
    [:thead
     [:tr [:th "Rule"] [:th "Status"] [:th "Cure"]]]
    [:tbody
     [:tr
      [:td "Zero Percent"]
      [:td (if (-> report :breakdown :zero-percent :valid?) "Pass" "Fail")]
      [:td (str "$" (-> report :breakdown :zero-percent :total-cure))]]
     [:tr
      [:td "Lender Credit"]
      [:td (if (-> report :breakdown :lender-credit :violated?) "Fail" "Pass")]
      [:td (str "$" (-> report :breakdown :lender-credit :required-cure))]]]]])
