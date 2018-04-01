-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
  (screenname, email, pass)
  VALUES (:screenname, :email, :pass)

-- :name update-user! :! :n
-- :doc update an existing user record with the given email
UPDATE users
  SET screenname = :screenname
  WHERE email = :email

-- :name get-user :? :1
-- :doc retrieve a user given the id.
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
