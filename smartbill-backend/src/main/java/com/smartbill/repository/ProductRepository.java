package com.smartbill.repository;

import com.smartbill.entity.Product;
import com.smartbill.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByUser(User user);

    Optional<Product> findByIdAndUser(Long id, User user);

    @Query("SELECT p FROM Product p WHERE p.user = :user AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Product> searchByUser(@Param("user") User user, @Param("search") String search);

    @Modifying
    @Query(value = "DELETE FROM invoice_item_imeis WHERE invoice_item_id IN (SELECT i.id FROM invoice_items i INNER JOIN invoices inv ON i.invoice_id = inv.id WHERE i.product_id = :productId AND inv.user_id = :userId)", nativeQuery = true)
    void deleteInvoiceItemImeisByProductIdAndUser(@Param("productId") Long productId, @Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM invoice_item_hsn_codes WHERE invoice_item_id IN (SELECT i.id FROM invoice_items i INNER JOIN invoices inv ON i.invoice_id = inv.id WHERE i.product_id = :productId AND inv.user_id = :userId)", nativeQuery = true)
    void deleteInvoiceItemHsnCodesByProductIdAndUser(@Param("productId") Long productId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM InvoiceItem i WHERE i.product.id = :productId AND i.invoice.user = :user")
    void deleteInvoiceItemsByProductIdAndUser(@Param("productId") Long productId, @Param("user") User user);
}
