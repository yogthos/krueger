(ns krueger.routes.home
  (:require
    [krueger.db.users :as user-db]
    [krueger.layout :as layout]
    [compojure.core :refer [defroutes GET]]
    [ring.util.http-response :as response]
    [clojure.java.io :as io]
    [clojure.tools.logging :as log]))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
  (GET "/" []
    (home-page))

  (GET "/registration/:token" [token :as req]
    (log/info "registration link activated for" token)
    (try
      (let [user (user-db/activate-user! token)]
        (-> (response/found "/#/registration")
            (assoc :session (assoc (:session req) :identity user))))
      (catch IllegalArgumentException e
        (layout/error-page
          {:status  400
           :title   "Registration failed!"
           :message (.getMessage e)})))))

