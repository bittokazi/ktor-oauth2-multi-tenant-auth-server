CREATE TABLE IF NOT EXISTS ${flyway:defaultSchema}.nonce_map (
  code VARCHAR(255) NOT NULL,
  nonce VARCHAR(255) NOT NULL,
  CONSTRAINT pk_nonce_map PRIMARY KEY (code)
);
