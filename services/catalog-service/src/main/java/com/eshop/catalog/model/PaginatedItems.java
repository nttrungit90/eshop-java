/**
 * Converted from: src/Catalog.API/Model/PaginatedItems.cs
 * .NET Class: eShop.Catalog.API.Model.PaginatedItems
 *
 * Generic wrapper for paginated API responses.
 */
package com.eshop.catalog.model;

import java.util.List;

public class PaginatedItems<T> {

    private final int pageIndex;
    private final int pageSize;
    private final long count;
    private final List<T> data;

    public PaginatedItems(int pageIndex, int pageSize, long count, List<T> data) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.count = count;
        this.data = data;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getCount() {
        return count;
    }

    public List<T> getData() {
        return data;
    }
}
