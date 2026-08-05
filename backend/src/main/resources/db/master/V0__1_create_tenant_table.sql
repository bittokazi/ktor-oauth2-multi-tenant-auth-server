CREATE TABLE IF NOT EXISTS ${flyway:defaultSchema}.tenant (
  id SERIAL NOT NULL,
  company_key varchar(255) NOT NULL,
  enabled boolean NOT NULL DEFAULT false,
  name varchar(255) NOT NULL,
  domain varchar(255),
  logo varchar(255),
  logo_absolute_path varchar(255),
  signin_btn_color varchar(255),
  reset_password_link varchar(255),
  create_account_link varchar(255),
  default_redirect_url varchar(255),
  enable_config_panel boolean NOT NULL DEFAULT false,
  enable_custom_template boolean NOT NULL DEFAULT false,
  custom_template_location varchar(255),
  CONSTRAINT tenant_company_key_key UNIQUE (company_key),
  CONSTRAINT tenant_name_key UNIQUE (name)
);

