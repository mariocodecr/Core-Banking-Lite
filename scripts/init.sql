-- Core Banking Lite — PostgreSQL initialization script
-- Runs once when the container is first created

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Test database for CI/CD pipelines
CREATE DATABASE corebanking_test
    WITH OWNER = corebanking
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_LOCALE = 'en_US.utf8'
    TEMPLATE = template0;
