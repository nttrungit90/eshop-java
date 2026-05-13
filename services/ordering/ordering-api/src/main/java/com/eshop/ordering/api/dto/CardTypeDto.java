package com.eshop.ordering.api.dto;

/** Wire shape for GET /api/orders/cardtypes. */
public class CardTypeDto {
    private long id;
    private String name;

    public CardTypeDto() {}
    public CardTypeDto(long id, String name) { this.id = id; this.name = name; }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
