/**
 * Converted from: src/Ordering.Domain/AggregatesModel/OrderAggregate/Address.cs
 * .NET Class: eShop.Ordering.Domain.AggregatesModel.OrderAggregate.Address
 *
 * Value object representing a shipping address.
 */
package com.eshop.ordering.domain.aggregates.order;

import com.eshop.ordering.domain.seedwork.ValueObject;
import jakarta.persistence.Embeddable;

import java.util.List;

@Embeddable
public class Address extends ValueObject {

    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;

    protected Address() {
    }

    public Address(String street, String city, String state, String country, String zipCode) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zipCode = zipCode;
    }

    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getCountry() { return country; }
    public String getZipCode() { return zipCode; }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(street, city, state, country, zipCode);
    }
}
