package com.eshop.catalog.dto;

import java.util.List;

public record PaginatedItemsDto<T>(
    int pageIndex,
    int pageSize,
    long count,
    List<T> data
) {}
