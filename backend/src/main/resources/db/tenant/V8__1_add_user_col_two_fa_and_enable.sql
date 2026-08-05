ALTER TABLE ${flyway:defaultSchema}.user ADD two_fa_enabled boolean DEFAULT FALSE;
ALTER TABLE ${flyway:defaultSchema}.user ADD user_enabled boolean DEFAULT TRUE;
