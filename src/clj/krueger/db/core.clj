(ns krueger.db.core
  (:require
    [alpha-id.core :refer [encode-64 decode-64]]
    [cheshire.core :refer [generate-string parse-string]]
    [clj-time.jdbc]
    [clojure.java.jdbc :as jdbc]
    [conman.core :as conman]
    [krueger.config :refer [env]]
    [mount.core :refer [defstate]])
  (:import org.postgresql.util.PGobject
           java.sql.Array
           clojure.lang.IPersistentMap
           clojure.lang.IPersistentVector
           [java.sql
            BatchUpdateException
            PreparedStatement]))

(defstate ^:dynamic *db*
  :start (conman/connect! {:jdbc-url (env :database-url)})
  :stop (conman/disconnect! *db*))

(conman/bind-connection *db*
                        "sql/attachments.sql"
                        "sql/comments.sql"
                        "sql/posts.sql"
                        "sql/users.sql")

(extend-protocol jdbc/IResultSetReadColumn
  Array
  (result-set-read-column [v _ _] (vec (.getArray v)))

  PGobject
  (result-set-read-column [pgobj _metadata _index]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (parse-string value true)
        "jsonb" (parse-string value true)
        "citext" (str value)
        value))))

(defn to-pg-json [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (generate-string value))))

(extend-type clojure.lang.IPersistentVector
  jdbc/ISQLParameter
  (set-parameter [v ^java.sql.PreparedStatement stmt ^long idx]
    (let [conn      (.getConnection stmt)
          meta      (.getParameterMetaData stmt)
          type-name (.getParameterTypeName meta idx)]
      (if-let [elem-type (when (= (first type-name) \_) (apply str (rest type-name)))]
        (.setObject stmt idx (.createArrayOf conn elem-type (to-array v)))
        (.setObject stmt idx (to-pg-json v))))))

(extend-protocol jdbc/ISQLValue
  IPersistentMap
  (sql-value [value] (to-pg-json value))
  IPersistentVector
  (sql-value [value] (to-pg-json value)))

(defstate posts-per-page
  :start (or (env :posts-per-page) 10))

;;; posts
(defn get-post-previews [category page]
  ;;TODO: filter by category
  ;;TODO: update to create a single sql query?
  {:pages (-> (total-posts {}) :count (/ posts-per-page) int)
   :page  (map
            #(update % :id encode-64)
            (post-previews {:limit posts-per-page :offset page}))})

(defn save-post! [post]
  (add-post!
    (merge {:text      nil
            :url       nil
            :preview   nil
            :upvotes   0
            :downvotes 0}
           post)))

(defn get-post-by-slug [slug]
  (let [id (decode-64 slug)]
    (-> (post-by-id {:id id})
        first
        (update :id encode-64)
        (assoc :comments (get-comments {:post id})))))

(defn upvote-post! [userid postid]
  (jdbc/with-db-transaction [t-conn *db*]
    (when-not (upvoted? {:userid userid :postid postid} t-conn)
      (upvote! {:id (decode-64 postid)} t-conn)
      (set-votes! {:upvoted true :downvoted false :userid userid :postid postid} t-conn))))

(defn downvote-post! [userid postid]
  (jdbc/with-db-transaction [t-conn *db*]
    (when-not (upvoted? {:userid userid :postid postid} t-conn)
      (downvote! {:id (decode-64 postid)} t-conn)
      (set-votes! {:upvoted false
                   :downvoted true
                   :userid userid
                   :postid postid}
                  t-conn))))

(defn add-post-comment! [comment]
  (add-comment! (assoc comment :upvotes 0 :downvotes 0)))
