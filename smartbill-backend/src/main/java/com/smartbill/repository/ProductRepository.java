package com.smartbill.repository;

import com.smartbill.entity.Product;
import com.smartbill.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = {"availableImeis"})
    @Query("SELECT p FROM Product p WHERE p.user = :user AND (p.sku IS NULL OR p.sku != 'MANUAL_ENTRY_DUMMY')")
    List<Product> findByUser(@Param("user") User user);

    @EntityGraph(attributePaths = {"availableImeis"})
    Optional<Product> findByIdAndUser(Long id, User user);
    
    Optional<Product> findBySkuAndUser(String sku, User user);

    @EntityGraph(attributePaths = {"availableImeis"})
    @Query("SELECT p FROM Product p WHERE p.user = :user AND (p.sku IS NULL OR p.sku != 'MANUAL_ENTRY_DUMMY') AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Product> searchByUser(@Param("user") User user, @Param("search") String search);
}
