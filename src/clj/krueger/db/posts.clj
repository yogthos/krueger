(ns krueger.db.posts
  (:require
    [krueger.db.core :refer [*db*]]
    [alpha-id.core :refer [encode-64 decode-64]]
    [camel-snake-kebab.extras :refer [transform-keys]]
    [camel-snake-kebab.core :refer [->kebab-case-keyword]]
    [cheshire.core :refer [generate-string parse-string]]
    [clojure.java.jdbc :as jdbc]
    [conman.core :as conman]
    [krueger.config :refer [env]]
    [mount.core :refer [defstate]]))

(conman/bind-connection *db*
                        "sql/comments.sql"
                        "sql/posts.sql"
                        "sql/tags.sql")

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
  (let [id   (decode-64 slug)]
    (-> (post-by-id {:id id})
        (assoc :id slug)
        (update :tags #(when % (tags-by-ids {:ids %})))
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
      (set-votes! {:upvoted   false
                   :downvoted true
                   :userid    userid
                   :postid    postid}
                  t-conn))))

(defn add-post-comment! [comment]
  (add-comment! (assoc comment :upvotes 0 :downvotes 0)))

#_(jdbc/query *db* ["select * from posts"])

#_(tags-by-ids {:ids (:tags (first (jdbc/query *db* ["select * from posts"])))})
#_ (tags-by-ids {:ids [1 2]})

#_(get-post-by-slug "5")

#_(save-post! {:title  "foo"
               :tags   [1 2]
               :text   "blah"
               :author "bob@bob.com"})
