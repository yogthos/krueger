-- :name add-comment! :<! :n
-- :doc add a new comment for a post
INSERT INTO comments
  (parent, author, content, post)
  VALUES (:parent, :author, :content, :post)
  RETURNING id

-- :name get-comments :? :*
-- :doc select all comments for the post
SELECT id, upvotes, downvotes, parent, author, content, timestamp FROM comments
  WHERE post = :post
