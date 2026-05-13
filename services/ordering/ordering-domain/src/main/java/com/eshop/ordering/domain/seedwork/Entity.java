package com.eshop.ordering.domain.seedwork;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Base for all aggregate entities. Subclasses own their own @Id field — this lets each entity
 * declare its own HiLo sequence (ordering.orderseq, ordering.orderitemseq, ordering.buyerseq,
 * ordering.paymentseq) matching the .NET schema.
 *
 * <p>Domain events are dispatched automatically by Spring Data JPA: after {@code repository.save(entity)},
 * Spring Data calls {@link #domainEvents()} (marked {@link DomainEvents @DomainEvents}), publishes each
 * event via the application's {@link org.springframework.context.ApplicationEventPublisher}, and then
 * invokes {@link #clearDomainEvents()} (marked {@link AfterDomainEventPublication}).
 *
 * <p>Listeners on the receiving side are typically {@code @Component} classes with
 * {@code @EventListener} methods; because the publish is synchronous and inside the originating
 * {@code @Transactional} boundary, the listener participates in the same database transaction —
 * matching .NET's {@code OrderingContext.SaveChangesAsync → MediatorExtension.DispatchDomainEventsAsync}.
 */
@MappedSuperclass
public abstract class Entity {

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public abstract Long getId();

    public boolean isTransient() {
        return getId() == null;
    }

    @DomainEvents
    public List<DomainEvent> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public void removeDomainEvent(DomainEvent event) {
        domainEvents.remove(event);
    }

    @AfterDomainEventPublication
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    /** Public read-only view for diagnostics — Spring Data uses {@link #domainEvents()}. */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity other = (Entity) o;
        if (isTransient() || other.isTransient()) return false;
        return Objects.equals(getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
