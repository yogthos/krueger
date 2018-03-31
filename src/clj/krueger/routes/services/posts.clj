(ns krueger.routes.services.posts
  (:require
    [schema.core :as s])
  (:import org.joda.time.DateTime))

(s/defschema Tag
  {:id s/Num :label String})

(s/defschema Comment
  {:id                      s/Num
   (s/optional-key :parent) s/Num
   :author                  String
   :content                 String
   :timestamp               DateTime
   :upvotes                 s/Num
   :downvotes               s/Num})

(s/defschema CommentSubmission
  {:post                    s/Num
   (s/optional-key :parent) s/Num
   :author                  String
   :content                 String})

;;post can either have a URL pointing to an external link or
;;text for self submition/question post, e.g: how do I setup a REPL
(s/defschema Post {:id                       String
                   :author                   String
                   :tags                     [Tag]
                   :title                    String
                   (s/optional-key :preview) (s/maybe String)
                   (s/optional-key :url)     (s/maybe String)
                   (s/optional-key :text)    (s/maybe String)
                   :upvotes                  s/Num
                   :downvotes                s/Num
                   :timestamp                DateTime
                   :comments                 [Comment]})

(s/defschema PostSubmission
  {:title                 String
   (s/optional-key :tags) [s/Num]
   (s/optional-key :url)  String
   (s/optional-key :text) String})

(s/defschema PostPreview
  {:id                       String
   :author                   String
   :tags                     [String]
   (s/optional-key :preview) String
   :title                    String
   (s/optional-key :url)     String
   :timestamp                DateTime
   :votes                    s/Num})

(s/defschema PostPreviews {:pages s/Num
                           :page  [PostPreview]})
