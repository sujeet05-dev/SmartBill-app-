package com.smartbill.repository;

import com.smartbill.entity.Invoice;
import com.smartbill.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Invoice> findByUserAndIsGstOrderByDateDesc(User user, Boolean isGst);
    
    @Query("SELECT i.invoiceNumber FROM Invoice i WHERE i.user = :user AND i.invoiceNumber NOT LIKE 'EST-%'")
    List<String> findAllGstInvoiceNumbersByUser(@Param("user") User user);
    
    @Query("SELECT i.invoiceNumber FROM Invoice i WHERE i.user = :user AND i.invoiceNumber LIKE 'EST-%'")
    List<String> findAllNonGstInvoiceNumbersByUser(@Param("user") User user);

    @Query(value = "SELECT invoice_number FROM invoices WHERE user_id = :userId AND invoice_number NOT LIKE 'EST-%' AND invoice_number NOT LIKE 'TEMP-%' ORDER BY LENGTH(invoice_number) DESC, invoice_number DESC LIMIT 1", nativeQuery = true)
    Optional<String> findMaxGstInvoiceNumberByUser(@Param("userId") Long userId);

    @Query(value = "SELECT invoice_number FROM invoices WHERE user_id = :userId AND invoice_number LIKE 'EST-%' AND invoice_number NOT LIKE 'TEMP-%' ORDER BY LENGTH(invoice_number) DESC, invoice_number DESC LIMIT 1", nativeQuery = true)
    Optional<String> findMaxNonGstInvoiceNumberByUser(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Invoice> findByIdAndUser(Long id, User user);

    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("SELECT i FROM Invoice i WHERE i.user = :user AND i.isGst = :isGst AND (LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.customerName) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY i.date DESC")
    List<Invoice> searchByUserAndIsGst(@Param("user") User user, @Param("search") String search, @Param("isGst") Boolean isGst);

    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("SELECT i FROM Invoice i WHERE i.user = :user AND i.isGst = true AND i.date >= :startDate AND i.date < :endDate ORDER BY i.date DESC")
    List<Invoice> findByMonthAndUser(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT EXTRACT(YEAR FROM i.date) AS yr, " +
                   "EXTRACT(MONTH FROM i.date) AS mo, " +
                   "COUNT(i.id) AS cnt, " +
                   "COALESCE(SUM(i.grand_total), 0) AS total_amt, " +
                   "COALESCE(SUM(i.total_gst), 0) AS total_gst " +
                   "FROM invoices i " +
                   "WHERE i.user_id = :userId AND i.is_gst = true " +
                   "GROUP BY EXTRACT(YEAR FROM i.date), EXTRACT(MONTH FROM i.date) " +
                   "ORDER BY yr DESC, mo DESC", nativeQuery = true)
    List<Object[]> getMonthlySummaryRaw(@Param("userId") Long userId);

    @Query("SELECT DISTINCT i FROM Invoice i JOIN i.items item WHERE item.product.id = :productId AND i.user = :user")
    List<Invoice> findByProductIdAndUser(@Param("productId") Long productId, @Param("user") User user);

    @Query("SELECT COALESCE(SUM(i.grandTotal), 0) FROM Invoice i WHERE i.user = :user")
    BigDecimal getTotalRevenueByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(item.quantity), 0) FROM Invoice i JOIN i.items item WHERE i.user = :user")
    Long getTotalItemsSoldByUser(@Param("user") User user);
}
