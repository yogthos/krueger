

-- :name add-comment! :! :n
-- :doc add a new comment for a post
INSERT INTO comments
  (parent, author, content, post)
  VALUES (:parent, :author, :content, :post)

