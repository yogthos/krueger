CREATE TABLE users
(email VARCHAR(50) PRIMARY KEY,
 sceenname VARCHAR(50),
 bio TEXT,
 admin BOOLEAN,
 last_login TIME,
 is_active BOOLEAN,
 pass VARCHAR(100));
