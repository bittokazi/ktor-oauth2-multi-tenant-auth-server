-- Creates user_two_fa_secret table
CREATE TABLE IF NOT EXISTS ${flyway:defaultSchema}.user_two_fa_secret (
  id SERIAL NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  secret varchar(255),
  scratch_codes varchar(255),
  CONSTRAINT pk_user_two_fa_secret PRIMARY KEY (id)
);

