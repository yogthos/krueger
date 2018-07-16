(ns krueger.middleware
  (:require
    [krueger.env :refer [defaults]]
    [cheshire.generate :as cheshire]
    [cognitect.transit :as transit]
    [clojure.tools.logging :as log]
    [krueger.layout :refer [error-page]]
    [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
    [muuntaja.core :as muuntaja]
    [muuntaja.format.json :refer [json-format]]
    [muuntaja.format.transit :as transit-format]
    [muuntaja.middleware :refer [wrap-format wrap-params]]
    [krueger.config :refer [env]]
    [krueger.layout :refer [*identity*]]
    [ring.middleware.flash :refer [wrap-flash]]
    [immutant.web.middleware :refer [wrap-session]]
    [ring.middleware.defaults :refer [site-defaults wrap-defaults]])
  (:import java.time.ZonedDateTime))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t (.getMessage t))
        (error-page {:status  500
                     :title   "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
    handler
    {:error-response
     (error-page
       {:status 403
        :title  "Invalid anti-forgery token"})}))

#_(def time-writer
    (transit/write-handler
      (constantly "m")
      (fn [v] (.getTime v) #_(-> ^ReadableInstant v .getMillis))
      (fn [v] (.toString (.getTime v)) #_(-> ^ReadableInstant v .getMillis .toString))))

#_(cheshire/add-encoder
    java.util.Date
    (fn [c jsonGenerator]
      (.writeString jsonGenerator (.toString (.geTime c)))))

(def restful-format-options
  muuntaja/default-options
  #_(update
      muuntaja/default-options
      :formats
      merge
      {"application/json"
       json-format

       "application/transit+json"
       {:decoder [(partial transit-format/make-transit-decoder :json)]
        :encoder [#(transit-format/make-transit-encoder
                     :json
                     (merge
                       %
                       {:handlers {java.util.Date time-writer}}))]}}))

(defn wrap-identity [handler]
  (fn [request]
    (binding [*identity* (get-in request [:session :identity])]
      (handler request))))

(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format restful-format-options))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-flash
      wrap-identity
      (wrap-session {:cookie-attrs {:http-only true}})
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (dissoc :session)))
      wrap-internal-error))
