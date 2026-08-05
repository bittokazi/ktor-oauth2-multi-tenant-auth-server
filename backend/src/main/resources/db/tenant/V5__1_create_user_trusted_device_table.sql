-- Creates user_trusted_device table
CREATE TABLE IF NOT EXISTS ${flyway:defaultSchema}.user_trusted_device (
  id SERIAL NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  instance_id varchar(255),
  device_ip varchar(255),
  user_agent varchar(255),
  created_date timestamp NOT NULL DEFAULT now(),
  updated_date timestamp,
  CONSTRAINT pk_user_trusted_device PRIMARY KEY (id)
);

