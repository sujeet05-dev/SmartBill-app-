package com.smartbill.repository;

import com.smartbill.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findAllByOrderByDateDesc();
    List<Invoice> findByInvoiceNumberContainingIgnoreCaseOrCustomerNameContainingIgnoreCaseOrderByDateDesc(String invoiceNumber, String customerName);
}
