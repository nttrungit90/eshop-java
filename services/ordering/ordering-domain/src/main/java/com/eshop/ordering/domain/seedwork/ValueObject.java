/**
 * Converted from: src/Ordering.Domain/SeedWork/ValueObject.cs
 * .NET Class: eShop.Ordering.Domain.SeedWork.ValueObject
 *
 * Base class for value objects following DDD patterns.
 */
package com.eshop.ordering.domain.seedwork;

import java.util.List;
import java.util.Objects;

public abstract class ValueObject {

    protected abstract List<Object> getEqualityComponents();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueObject that = (ValueObject) o;
        return getEqualityComponents().equals(that.getEqualityComponents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEqualityComponents().toArray());
    }
}
