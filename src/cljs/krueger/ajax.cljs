(ns krueger.ajax
  (:require [ajax.core :as ajax]
            [re-frame.core :as rf]))

(defn local-uri? [{:keys [uri]}]
  (not (re-find #"^\w+?://" uri)))

(defn default-headers [request]
  (if (local-uri? request)
    (update request :headers #(merge {"x-csrf-token" js/csrfToken} %))
    request))

(defn load-interceptors! []
  (swap! ajax/default-interceptors
         conj
         (ajax/to-interceptor {:name    "default headers"
                               :request default-headers})))

(def http-methods
  {:get    ajax/GET
   :post   ajax/POST
   :put    ajax/PUT
   :delete ajax/DELETE})

(rf/reg-event-fx
  :http/response
  (fn [{db :db} [_ resource-id event]]
    {:db (update db :http/loading disj resource-id)
     :dispatch event}))

(rf/reg-sub
  :http/loading?
  (fn [db [_ resource-id]]
    (boolean (some #{resource-id} (:http/loading db)))))

(rf/reg-event-db
  :http/loading
  (fn [db [_ resource-id]]
    (update db :http/loading (fnil conj #{}) resource-id)))

(rf/reg-fx
  :http
  (fn [{:keys [method
               url
               success-event
               error-event
               params
               ajax-map
               resource-id]
        :or   {error-event [:common/set-error]
               ajax-map    {}}}]
    (when resource-id
      (rf/dispatch [:http/loading resource-id]))
    ((http-methods method)
      url (merge
            {:params        params
             :handler       (fn [response]
                              (rf/dispatch
                                [:http/response
                                 resource-id
                                 (when success-event
                                   (conj success-event response))]))
             :error-handler (fn [error]
                              (rf/dispatch
                                [:http/response
                                 resource-id
                                 (when success-event
                                   (conj error-event error))]))}
            ajax-map))))