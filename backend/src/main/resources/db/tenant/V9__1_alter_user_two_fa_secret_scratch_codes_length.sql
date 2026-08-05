-- Alters user_two_fa_secret.scratch_codes column length to 2000
-- Uses the Flyway default schema placeholder to work across environments
ALTER TABLE ${flyway:defaultSchema}.user_two_fa_secret
    ALTER COLUMN scratch_codes TYPE varchar(2000);
