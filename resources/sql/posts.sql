-- :name add-post! :! :n
-- :doc create a new post row
INSERT INTO posts
  (title, author, preview, url, text, upvotes, downvotes)
  VALUES (:title, :author, :preview, :url, :text, :upvotes, :downvotes)

-- :name post-by-id :? :1
-- :doc retrieve a post using the id
SELECT id, author, title, preview, url, text, upvotes, downvotes, timestamp
  FROM posts
  WHERE id = :id

-- :name post-previews :? :*
-- :doc retrieve previews given the offset
SELECT id, author, title, preview, url, upvotes, downvotes, timestamp
  FROM posts
  LIMIT :limit OFFSET :offset

-- :name total-posts :? :1
-- :doc count posts
SELECT count(*)
  FROM posts

-- :name upvote! :! :n
-- :doc increase post votes
UPDATE posts
  SET upvotes = upvotes + 1
  WHERE id = :id

--:name upvoted? :? :1
-- :doc selects the upvoted flag for the user and post
SELECT upvoted
  FROM post_votes
  WHERE userid = :userid
  AND   postid = :postid

-- :name downvote! :! :n
-- :doc increase post votes
UPDATE posts
  SET downvotes = downvotes + 1
  WHERE id = :id

--:name downvoted? :? :1
-- :doc selects the downvoted flag for the user and post
SELECT downvoted
  FROM post_votes
  WHERE userid = :userid
  AND   postid = :postid

--:name set-votes! :! :n
-- :doc sets the votes for the user and post
UPDATE votes
  SET upvoted = :upvoted, downvoted = :downvoted
  WHERE userid = :userid
  AND   postid = :postid

-- :name get-comments :? :*
-- :doc select all comments for the post
SELECT post, id, upvotes, downvotes, parent, author, content, timestamp FROM comments
  WHERE post = :post
