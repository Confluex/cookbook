CREATE TABLE IF NOT EXISTS user (
  user_name  VARCHAR(16) PRIMARY KEY,
  last_name  VARCHAR(32) NOT NULL,
  first_name VARCHAR(32) NOT NULL
)
AS SELECT
     user_name,
     last_name,
     first_name
   FROM CSVREAD('classpath:/jdbc/users.csv');
