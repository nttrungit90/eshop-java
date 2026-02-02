/**
 * Converted from: src/Ordering.Domain/SeedWork/Entity.cs
 * .NET Class: eShop.Ordering.Domain.SeedWork.Entity
 *
 * Base class for all domain entities.
 */
package com.eshop.ordering.domain.seedwork;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@MappedSuperclass
public abstract class Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();

    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    public boolean isTransient() {
        return this.id == null;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public void removeDomainEvent(DomainEvent event) {
        domainEvents.remove(event);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        if (isTransient() || entity.isTransient()) return false;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
