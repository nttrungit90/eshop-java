package com.eshop.ordering.api.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Wire shape matches .NET WebApp's CreateOrderRequest (BasketState.cs line 158):
 * UserId, UserName, City, Street, State, Country, ZipCode,
 * CardNumber, CardHolderName, CardExpiration, CardSecurityNumber, CardTypeId,
 * Buyer, Items: List&lt;BasketItem&gt;.
 * .NET serializes camelCase via JsonSerializerDefaults.Web; Jackson here reads camelCase by default.
 */
public class CreateOrderRequest {

    private String userId;
    private String userName;

    @NotBlank private String city;
    @NotBlank private String street;
    @NotBlank private String state;
    @NotBlank private String country;
    @NotBlank private String zipCode;

    @NotBlank
    @Size(min = 12, max = 19, message = "CardNumber length must be 12–19")
    private String cardNumber;

    @NotBlank
    @Size(max = 200)
    private String cardHolderName;

    @NotNull
    @FutureOrPresent(message = "CardExpiration must be today or later")
    private Instant cardExpiration;

    @NotBlank
    @Size(min = 3, max = 4)
    private String cardSecurityNumber;

    @Min(value = 1, message = "CardTypeId must be a valid card type id")
    private int cardTypeId;

    private String buyer;

    @NotEmpty
    private List<BasketItem> items;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    public Instant getCardExpiration() { return cardExpiration; }
    public void setCardExpiration(Instant cardExpiration) { this.cardExpiration = cardExpiration; }
    public String getCardSecurityNumber() { return cardSecurityNumber; }
    public void setCardSecurityNumber(String cardSecurityNumber) { this.cardSecurityNumber = cardSecurityNumber; }
    public int getCardTypeId() { return cardTypeId; }
    public void setCardTypeId(int cardTypeId) { this.cardTypeId = cardTypeId; }

    public String getBuyer() { return buyer; }
    public void setBuyer(String buyer) { this.buyer = buyer; }

    public List<BasketItem> getItems() { return items; }
    public void setItems(List<BasketItem> items) { this.items = items; }

    public static class BasketItem {
        private String id;
        private int productId;
        private String productName;
        private BigDecimal unitPrice;
        private BigDecimal oldUnitPrice;
        private int quantity;
        private String pictureUrl;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getOldUnitPrice() { return oldUnitPrice; }
        public void setOldUnitPrice(BigDecimal oldUnitPrice) { this.oldUnitPrice = oldUnitPrice; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getPictureUrl() { return pictureUrl; }
        public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }
    }
}
