-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
  (id, email, pass, token, active)
  VALUES (:id, :email, :pass, :token, :active)

-- :name get-user-by-id :? :1
-- :doc retrieve a user given the id.
SELECT * FROM users
  WHERE id = :id

-- :name get-user-by-email :? :1
-- :doc retrieve a user given the email.
SELECT * FROM users
  WHERE email = :email

-- :name set-user-token! :! :n
-- :doc sets the registration token for the user
update users set token=:token where email = :email

-- :name get-unregistered-user :? :1
-- :doc selects the email for a users that isn't active
select email from users where email=? and active is not true

-- :name get-user-by-token :? :1
-- :doc selects the user given the registration token
select * from users where token=?

-- :name finish-registration :! :n
-- :doc activates the account and removes the registration token
update users set token = null, active = true where token = ?

-- :name user-follows* :? :*
-- :doc ids of users followed by userid
select follows from user_graph where userid = :userid

-- :name user-followed-by* :? :*
-- :doc ids of users who follow userid
select userid from user_graph where follows = :userid