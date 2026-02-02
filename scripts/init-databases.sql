-- Database initialization script for eShop
-- Creates all required databases with pgvector extension

-- Create databases
CREATE DATABASE catalogdb;
CREATE DATABASE identitydb;
CREATE DATABASE orderingdb;
CREATE DATABASE webhooksdb;

-- Enable pgvector extension for AI semantic search in catalog
\c catalogdb;
CREATE EXTENSION IF NOT EXISTS vector;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE catalogdb TO postgres;
GRANT ALL PRIVILEGES ON DATABASE identitydb TO postgres;
GRANT ALL PRIVILEGES ON DATABASE orderingdb TO postgres;
GRANT ALL PRIVILEGES ON DATABASE webhooksdb TO postgres;
