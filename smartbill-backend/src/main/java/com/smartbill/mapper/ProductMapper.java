package com.smartbill.mapper;

import com.smartbill.dto.ProductDto;
import com.smartbill.entity.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ProductMapper {

    public ProductDto toDto(Product product) {
        if (product == null) return null;
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBrand(product.getBrand());
        dto.setCategory(product.getCategory());
        dto.setPrice(product.getPrice());
        dto.setGstPercentage(product.getGstPercentage());
        dto.setStock(product.getStock());
        dto.setSku(product.getSku());
        dto.setAvailableImeis(product.getAvailableImeis() != null ? new ArrayList<>(product.getAvailableImeis()) : null);
        dto.setUnit(product.getUnit());
        dto.setHsnCode(product.getHsnCode());
        return dto;
    }

    public Product toEntity(ProductDto dto) {
        if (dto == null) return null;
        Product product = new Product();
        if (dto.getId() != null && dto.getId() != 0) {
            product.setId(dto.getId());
        }
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBrand(dto.getBrand());
        product.setCategory(dto.getCategory());
        product.setPrice(dto.getPrice());
        product.setGstPercentage(dto.getGstPercentage());
        product.setStock(dto.getStock());
        product.setSku(dto.getSku());
        product.setAvailableImeis(dto.getAvailableImeis() != null ? new ArrayList<>(dto.getAvailableImeis()) : new ArrayList<>());
        product.setUnit(dto.getUnit() != null ? dto.getUnit() : "PCS");
        product.setHsnCode(dto.getHsnCode());
        return product;
    }
}
