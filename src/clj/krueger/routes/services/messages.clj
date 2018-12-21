(ns krueger.routes.services.messages
  (:require
    [schema.core :as s]))

(s/defschema Message
  {:author s/Str
   :recipient s/Str
   :content s/Str})