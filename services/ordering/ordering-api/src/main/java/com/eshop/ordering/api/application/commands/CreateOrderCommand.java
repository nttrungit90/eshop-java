package com.eshop.ordering.api.application.commands;

import com.eshop.ordering.api.application.commandbus.Command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Mirrors .NET CreateOrderCommand. */
public final class CreateOrderCommand implements Command<Boolean> {

    private final String userId;
    private final String userName;
    private final String city;
    private final String street;
    private final String state;
    private final String country;
    private final String zipCode;
    private final String cardNumber;
    private final String cardHolderName;
    private final Instant cardExpiration;
    private final String cardSecurityNumber;
    private final int cardTypeId;
    private final List<OrderItemDto> items;

    public CreateOrderCommand(String userId, String userName, String city, String street, String state,
                              String country, String zipCode, String cardNumber, String cardHolderName,
                              Instant cardExpiration, String cardSecurityNumber, int cardTypeId,
                              List<OrderItemDto> items) {
        this.userId = userId;
        this.userName = userName;
        this.city = city;
        this.street = street;
        this.state = state;
        this.country = country;
        this.zipCode = zipCode;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.cardExpiration = cardExpiration;
        this.cardSecurityNumber = cardSecurityNumber;
        this.cardTypeId = cardTypeId;
        this.items = items;
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getCity() { return city; }
    public String getStreet() { return street; }
    public String getState() { return state; }
    public String getCountry() { return country; }
    public String getZipCode() { return zipCode; }
    public String getCardNumber() { return cardNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public Instant getCardExpiration() { return cardExpiration; }
    public String getCardSecurityNumber() { return cardSecurityNumber; }
    public int getCardTypeId() { return cardTypeId; }
    public List<OrderItemDto> getItems() { return items; }

    public static final class OrderItemDto {
        private final long productId;
        private final String productName;
        private final BigDecimal unitPrice;
        private final BigDecimal discount;
        private final String pictureUrl;
        private final int units;

        public OrderItemDto(long productId, String productName, BigDecimal unitPrice,
                            BigDecimal discount, String pictureUrl, int units) {
            this.productId = productId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.discount = discount;
            this.pictureUrl = pictureUrl;
            this.units = units;
        }

        public long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getDiscount() { return discount; }
        public String getPictureUrl() { return pictureUrl; }
        public int getUnits() { return units; }
    }
}
