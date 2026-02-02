/**
 * Converted from: src/Basket.API/Model/CustomerBasket.cs
 * .NET Class: eShop.Basket.API.Model.CustomerBasket
 *
 * Customer basket model stored in Redis.
 */
package com.eshop.basket.model;

import java.util.ArrayList;
import java.util.List;

public class CustomerBasket {

    private String buyerId;
    private List<BasketItem> items = new ArrayList<>();

    public CustomerBasket() {
    }

    public CustomerBasket(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public List<BasketItem> getItems() {
        return items;
    }

    public void setItems(List<BasketItem> items) {
        this.items = items;
    }
}
