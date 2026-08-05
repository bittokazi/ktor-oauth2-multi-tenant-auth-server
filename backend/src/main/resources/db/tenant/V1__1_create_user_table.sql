CREATE TABLE IF NOT EXISTS ${flyway:defaultSchema}.user (
  id VARCHAR(255) NOT NULL UNIQUE,
  email varchar(255) NOT NULL,
  first_name varchar(255) NOT NULL,
  last_name varchar(255) NOT NULL,
  password varchar(255) NOT NULL,
  CONSTRAINT user_name_key UNIQUE (email)
);
