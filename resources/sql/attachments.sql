--:name save-file! :! :n
-- saves a file to the database
insert into files
(type, name, data)
values (:type, :name, :data)

--:name load-file-data :? :1
-- retrieve file data by name
select type, data
from files
where name = :name

-- :name delete-file<! :! :1
-- :doc removes file from the database
delete from files
where name = :name;
