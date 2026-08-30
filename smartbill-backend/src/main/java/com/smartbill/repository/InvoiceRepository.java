package com.smartbill.repository;

import com.smartbill.entity.Invoice;
import com.smartbill.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    List<Invoice> findByUserAndIsGstOrderByDateDesc(User user, Boolean isGst);
    
    @Query("SELECT i.invoiceNumber FROM Invoice i WHERE i.user = :user AND i.invoiceNumber NOT LIKE 'EST-%'")
    List<String> findAllGstInvoiceNumbersByUser(@Param("user") User user);
    
    @Query("SELECT i.invoiceNumber FROM Invoice i WHERE i.user = :user AND i.invoiceNumber LIKE 'EST-%'")
    List<String> findAllNonGstInvoiceNumbersByUser(@Param("user") User user);

    Optional<Invoice> findByIdAndUser(Long id, User user);

    @Query("SELECT i FROM Invoice i WHERE i.user = :user AND i.isGst = :isGst AND (LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.customerName) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY i.date DESC")
    List<Invoice> searchByUserAndIsGst(@Param("user") User user, @Param("search") String search, @Param("isGst") Boolean isGst);

    @Query("SELECT DISTINCT i FROM Invoice i JOIN i.items item WHERE item.product.id = :productId AND i.user = :user")
    List<Invoice> findByProductIdAndUser(@Param("productId") Long productId, @Param("user") User user);

    @Query("SELECT COALESCE(SUM(i.grandTotal), 0) FROM Invoice i WHERE i.user = :user")
    BigDecimal getTotalRevenueByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(item.quantity), 0) FROM Invoice i JOIN i.items item WHERE i.user = :user")
    Long getTotalItemsSoldByUser(@Param("user") User user);
}
