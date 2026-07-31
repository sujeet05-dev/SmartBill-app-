package com.smartbill.mapper;

import com.smartbill.dto.ProductDto;
import com.smartbill.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDto toDto(Product product) {
        if (product == null) return null;
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setCategory(product.getCategory());
        dto.setPrice(product.getPrice());
        dto.setGstPercentage(product.getGstPercentage());
        dto.setStock(product.getStock());
        dto.setSku(product.getSku());
        return dto;
    }

    public Product toEntity(ProductDto dto) {
        if (dto == null) return null;
        Product product = new Product();
        if (dto.getId() != null && dto.getId() != 0) {
            product.setId(dto.getId());
        }
        product.setName(dto.getName());
        product.setBrand(dto.getBrand());
        product.setCategory(dto.getCategory());
        product.setPrice(dto.getPrice());
        product.setGstPercentage(dto.getGstPercentage());
        product.setStock(dto.getStock());
        product.setSku(dto.getSku());
        return product;
    }
}
