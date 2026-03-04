(ns gritum.external.llm
  (:require
   [clj-http.client :as client]
   [cheshire.core :as json]
   [clojure.java.io :as io]))

(defn get-all-models [api-key]
  (let [url (str "https://generativelanguage.googleapis.com/v1beta/models?key=" api-key)]
    (try
      (-> (client/get url {:as :json})
          (get-in [:body :models]))
      (catch Exception e
        (println "Could not even list models. Error:" (:body (ex-data e)))))))

(comment
  (require '[gritum.config :as configs])
  (get-all-models (:ai-api-key (configs/llm-cfg))))

(defn get-prompt [file]
  (let [path (str "prompts/" file)]
    (slurp (io/resource path))))

(defn ->base64 [path]
  (if-let [input (or (io/resource path)
                     (let [f (io/file path)]
                       (when (.exists f) f)))]
    (with-open [in (io/input-stream input)
                out (java.io.ByteArrayOutputStream.)]
      (io/copy in out)
      (.encodeToString
       (java.util.Base64/getEncoder)
       (.toByteArray out)))
    (throw (ex-info "Cannot find the file." {:path path}))))

(defn call!
  ([api-key ai-model prompt schema]
   (call! api-key ai-model prompt schema nil))
  ([api-key ai-model prompt schema pdf-file-path]
   (let [url (str "https://generativelanguage.googleapis.com/v1beta/models/"
                  ai-model ":generateContent?key=" api-key)
         parts (into [{:text prompt}]
                     (when pdf-file-path
                       [{:inline_data {:mime_type "application/pdf"
                                       :data (->base64 pdf-file-path)}}]))
         payload {:contents [{:parts parts}]
                  :generationConfig {:response_mime_type "application/json"
                                     :response_schema schema}}]
     (try
       (-> (client/post url {:body (json/generate-string payload)
                             :content-type :json
                             :as :json})
           :body :candidates first :content :parts first :text
           (json/parse-string true))
       (catch Exception e
    ;; ex-data를 통해 서버가 보낸 실제 JSON 에러 메시지를 출력합니다.
         (println "🚨 API Error Body:" (:body (ex-data e)))
         (throw e))))))
