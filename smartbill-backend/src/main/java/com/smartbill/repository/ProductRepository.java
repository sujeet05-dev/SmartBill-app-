package com.smartbill.repository;

import com.smartbill.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrSkuContainingIgnoreCase(String name, String brand, String sku);

    @Modifying
    @Query("DELETE FROM InvoiceItem i WHERE i.product.id = :productId")
    void deleteInvoiceItemsByProductId(@Param("productId") Long productId);
}
