-- Make default clients public
UPDATE ${flyway:defaultSchema}.oauth_clients
SET client_type = 'public'
WHERE is_default = TRUE;
