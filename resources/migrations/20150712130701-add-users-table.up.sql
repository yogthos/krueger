CREATE TABLE users
(email VARCHAR(50) PRIMARY KEY,
 screenname VARCHAR(50),
 bio TEXT,
 admin BOOLEAN,
 moderator BOOLEAN,
 token TEXT,
 last_login TIME,
 active BOOLEAN,
 pass VARCHAR(100));
