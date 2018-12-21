CREATE TABLE messages
(id SERIAL PRIMARY KEY,
 recipient VARCHAR(50),
 author VARCHAR(50),
 unread BOOLEAN,
 content TEXT);