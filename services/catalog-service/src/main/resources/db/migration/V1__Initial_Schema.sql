-- Flyway Migration V1: Initial Schema
-- Converted from .NET EF Core Migrations:
--   - 20231009153249_Initial.cs
--   - 20231018163051_RemoveHiLoAndIndexCatalogName.cs
--   - 20231026091140_Outbox.cs
--
-- Uses PascalCase table/column names to match .NET EF Core schema.

-- Enable pgvector extension for AI semantic search
CREATE EXTENSION IF NOT EXISTS vector;

-- ===========================================
-- Catalog Tables (matching .NET schema exactly)
-- ===========================================

CREATE TABLE IF NOT EXISTS "CatalogBrand" (
    "Id" SERIAL PRIMARY KEY,
    "Brand" VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS "CatalogType" (
    "Id" SERIAL PRIMARY KEY,
    "Type" VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS "Catalog" (
    "Id" SERIAL PRIMARY KEY,
    "Name" VARCHAR(50) NOT NULL,
    "Description" TEXT,
    "Price" NUMERIC NOT NULL,
    "PictureFileName" TEXT,
    "CatalogTypeId" INTEGER NOT NULL REFERENCES "CatalogType"("Id") ON DELETE CASCADE,
    "CatalogBrandId" INTEGER NOT NULL REFERENCES "CatalogBrand"("Id") ON DELETE CASCADE,
    "AvailableStock" INTEGER NOT NULL DEFAULT 0,
    "RestockThreshold" INTEGER NOT NULL DEFAULT 0,
    "MaxStockThreshold" INTEGER NOT NULL DEFAULT 0,
    "OnReorder" BOOLEAN NOT NULL DEFAULT FALSE,
    "Embedding" vector(384)
);

-- Indexes (matching .NET naming)
CREATE INDEX "IX_Catalog_Name" ON "Catalog"("Name");
CREATE INDEX "IX_Catalog_CatalogBrandId" ON "Catalog"("CatalogBrandId");
CREATE INDEX "IX_Catalog_CatalogTypeId" ON "Catalog"("CatalogTypeId");

-- ===========================================
-- Integration Event Log (Outbox Pattern)
-- ===========================================

CREATE TABLE IF NOT EXISTS "IntegrationEventLog" (
    "EventId" UUID PRIMARY KEY,
    "EventTypeName" VARCHAR(255) NOT NULL,
    "State" INTEGER NOT NULL DEFAULT 0,
    "TimesSent" INTEGER NOT NULL DEFAULT 0,
    "CreationTime" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    "Content" TEXT NOT NULL,
    "TransactionId" VARCHAR(36) NOT NULL
);

CREATE INDEX "IX_IntegrationEventLog_State" ON "IntegrationEventLog"("State");
CREATE INDEX "IX_IntegrationEventLog_TransactionId" ON "IntegrationEventLog"("TransactionId");
