-- Initialise databases on first start.
--
-- This script runs ONLY when Postgres boots against an empty data directory
-- (Docker's standard /docker-entrypoint-initdb.d behavior). If the persistent
-- volume already contains data — e.g. the existing `eshop-postgres-data`
-- volume that was previously managed by .NET Aspire — this file is skipped
-- and existing databases (with their data) are preserved.
--
-- Schema for each database is created on demand by its owning service:
--   catalogdb   → catalog-service (Hibernate ddl-auto: update)
--   orderingdb  → originally .NET Ordering EF migrations; now read by the
--                 migrated Java ordering-api with ddl-auto: none. For a fully
--                 fresh setup the ordering schema (with HiLo sequences and
--                 PascalCase columns) must be created by running .NET EF
--                 migrations once, or by importing a dump.
--   webhooksdb  → webhooks-service (Hibernate ddl-auto: update)

CREATE DATABASE catalogdb;
CREATE DATABASE orderingdb;
CREATE DATABASE webhooksdb;
