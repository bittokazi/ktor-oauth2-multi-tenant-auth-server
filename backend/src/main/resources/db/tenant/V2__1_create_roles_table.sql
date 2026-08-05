CREATE TABLE IF NOT EXISTS ${flyway:defaultSchema}.role (
  id VARCHAR(255) NOT NULL UNIQUE,
  name varchar(255) NOT NULL,
  role_key varchar(255) NOT NULL,
  CONSTRAINT role_role_key UNIQUE (role_key)
);
