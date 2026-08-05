package com.smartbill.service;

import com.smartbill.dto.ProductDto;
import com.smartbill.entity.Product;
import com.smartbill.entity.User;
import com.smartbill.mapper.ProductMapper;
import com.smartbill.repository.ProductRepository;
import com.smartbill.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final SecurityUtils securityUtils;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper, SecurityUtils securityUtils) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.securityUtils = securityUtils;
    }

    public List<ProductDto> getAllProducts(String search) {
        User currentUser = securityUtils.getCurrentUser();
        List<Product> products;
        if (search != null && !search.trim().isEmpty()) {
            products = productRepository.searchByUser(currentUser, search.trim());
        } else {
            products = productRepository.findByUser(currentUser);
        }
        return products.stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    public ProductDto createProduct(ProductDto productDto) {
        User currentUser = securityUtils.getCurrentUser();
        Product product = productMapper.toEntity(productDto);
        product.setUser(currentUser);
        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        User currentUser = securityUtils.getCurrentUser();
        Product existing = productRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        existing.setName(productDto.getName());
        existing.setDescription(productDto.getDescription());
        existing.setBrand(productDto.getBrand());
        existing.setCategory(productDto.getCategory());
        existing.setPrice(productDto.getPrice());
        existing.setGstPercentage(productDto.getGstPercentage());
        existing.setStock(productDto.getStock());
        existing.setSku(productDto.getSku());
        if (productDto.getAvailableImeis() != null) {
            existing.getAvailableImeis().clear();
            existing.getAvailableImeis().addAll(productDto.getAvailableImeis());
        } else {
            existing.getAvailableImeis().clear();
        }
        existing.setUnit(productDto.getUnit() != null ? productDto.getUnit() : "PCS");

        Product saved = productRepository.save(existing);
        return productMapper.toDto(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Product existing = productRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productRepository.deleteInvoiceItemsByProductIdAndUser(existing.getId(), currentUser);
        productRepository.delete(existing);
    }
}
