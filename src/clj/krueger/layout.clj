(ns krueger.layout
  (:require
    [krueger.config :refer [env]]
    [selmer.parser :as parser]
    [ring.util.http-response :refer [content-type ok]]
    [ring.util.anti-forgery :refer [anti-forgery-field]]
    [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]))


(declare ^:dynamic *identity*)
(parser/set-resource-path! (clojure.java.io/resource "html"))
(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))

(defn render
  "renders the HTML template located relative to resources/html"
  [template & [params]]
  (content-type
    (ok
      (parser/render-file
        template
        (assoc params
          :user *identity*
          :page template
          :locale (:locale env)
          :csrf-token *anti-forgery-token*)))
    "text/html; charset=utf-8"))

(defn error-page
  "error-details should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)

   returns a response map with the error page as the body
   and the status specified by the status key"
  [error-details]
  {:status  (:status error-details)
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (parser/render-file "error.html" error-details)})
