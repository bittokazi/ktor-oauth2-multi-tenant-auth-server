CREATE TABLE IF NOT EXISTS ${flyway:defaultSchema}.user_role (
  user_id VARCHAR(255) NOT NULL,
  role_id VARCHAR(255) NOT NULL,
  CONSTRAINT uq_userid_roleid UNIQUE (user_id, role_id)
);
