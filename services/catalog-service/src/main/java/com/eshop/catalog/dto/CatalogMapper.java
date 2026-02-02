package com.eshop.catalog.dto;

import com.eshop.catalog.model.CatalogBrand;
import com.eshop.catalog.model.CatalogItem;
import com.eshop.catalog.model.CatalogType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class CatalogMapper {

    @Mapping(target = "catalogTypeId", source = "catalogType.id")
    @Mapping(target = "catalogType", source = "catalogType")
    @Mapping(target = "catalogBrandId", source = "catalogBrand.id")
    @Mapping(target = "catalogBrand", source = "catalogBrand")
    public abstract CatalogItemDto toDto(CatalogItem item);

    public abstract List<CatalogItemDto> toDtoList(List<CatalogItem> items);

    public abstract CatalogTypeDto toDto(CatalogType type);

    public abstract List<CatalogTypeDto> toTypeDtoList(List<CatalogType> types);

    public abstract CatalogBrandDto toDto(CatalogBrand brand);

    public abstract List<CatalogBrandDto> toBrandDtoList(List<CatalogBrand> brands);
}
