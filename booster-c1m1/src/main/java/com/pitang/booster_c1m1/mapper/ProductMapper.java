package com.pitang.booster_c1m1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.pitang.booster_c1m1.domain.Product;
import com.pitang.booster_c1m1.dto.CreateProductDTO;
import com.pitang.booster_c1m1.dto.ProductDTO;

@Mapper
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toProduct(CreateProductDTO createProductDTO);

    ProductDTO toDto(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateProductFromDTO(CreateProductDTO createProductDTO, @MappingTarget Product product);
}