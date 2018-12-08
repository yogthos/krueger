(ns krueger.routes.services.posts
  (:require
    [schema.core :as s])
  (:import java.util.Date))

(s/defschema Tag
  {:id          s/Num
   :label       s/Str
   :description s/Str})

(s/defschema Comment
  {:id                      s/Num
   (s/optional-key :parent) (s/maybe s/Num)
   :author                  s/Str
   :content                 s/Str
   :timestamp               Date
   :upvotes                 (s/maybe s/Num)
   :downvotes               (s/maybe s/Num)})

(s/defschema CommentSubmission
  {:post                    s/Str
   (s/optional-key :parent) (s/maybe s/Num)
   :content                 s/Str})

(s/defschema SubmissionResult
  {:id s/Num})

;;post can either have a URL pointing to an external link or
;;text for self submition/question post, e.g: how do I setup a REPL
(s/defschema Post {:id                       s/Str
                   :author                   s/Str
                   :tags                     [Tag]
                   :title                    String
                   (s/optional-key :preview) (s/maybe s/Str)
                   (s/optional-key :url)     (s/maybe s/Str)
                   (s/optional-key :text)    (s/maybe s/Str)
                   :timestamp                Date
                   :comments                 [Comment]})

(s/defschema PostSubmission
  {:title                 s/Str
   (s/optional-key :tags) [s/Num]
   (s/optional-key :url)  s/Str
   (s/optional-key :text) s/Str})

(s/defschema PostPreview
  {:id                       s/Str
   :author                   s/Str
   :tags                     [s/Num]
   (s/optional-key :preview) (s/maybe s/Str)
   :title                    s/Str
   (s/optional-key :url)     (s/maybe s/Str)
   :timestamp                Date})

(s/defschema PostPreviews {:posts [PostPreview]})
