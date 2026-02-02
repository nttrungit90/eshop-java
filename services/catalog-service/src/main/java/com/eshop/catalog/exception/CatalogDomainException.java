/**
 * Converted from: src/Catalog.API/Infrastructure/Exceptions/CatalogDomainException.cs
 * .NET Class: eShop.Catalog.API.Infrastructure.Exceptions.CatalogDomainException
 *
 * Domain exception for catalog operations.
 */
package com.eshop.catalog.exception;

public class CatalogDomainException extends RuntimeException {

    public CatalogDomainException() {
        super();
    }

    public CatalogDomainException(String message) {
        super(message);
    }

    public CatalogDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
