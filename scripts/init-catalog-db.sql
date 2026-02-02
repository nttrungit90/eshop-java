-- Database initialization script for catalog-service dependencies
-- Creates catalogdb and identitydb with pgvector extension

-- Create databases
CREATE DATABASE catalogdb;
CREATE DATABASE identitydb;

-- Enable pgvector extension for AI semantic search in catalog
\c catalogdb;
CREATE EXTENSION IF NOT EXISTS vector;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE catalogdb TO postgres;
GRANT ALL PRIVILEGES ON DATABASE identitydb TO postgres;
