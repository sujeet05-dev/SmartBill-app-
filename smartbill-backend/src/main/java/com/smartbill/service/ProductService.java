package com.smartbill.service;

import com.smartbill.dto.ProductDto;
import com.smartbill.entity.Product;
import com.smartbill.mapper.ProductMapper;
import com.smartbill.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public List<ProductDto> getAllProducts(String search) {
        List<Product> products;
        if (search != null && !search.trim().isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrSkuContainingIgnoreCase(search, search, search);
        } else {
            products = productRepository.findAll();
        }
        return products.stream().map(productMapper::toDto).collect(Collectors.toList());
    }

    public ProductDto createProduct(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        existing.setName(productDto.getName());
        existing.setBrand(productDto.getBrand());
        existing.setCategory(productDto.getCategory());
        existing.setPrice(productDto.getPrice());
        existing.setGstPercentage(productDto.getGstPercentage());
        existing.setStock(productDto.getStock());
        existing.setSku(productDto.getSku());

        Product saved = productRepository.save(existing);
        return productMapper.toDto(saved);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }
}
