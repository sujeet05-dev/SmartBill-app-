package com.smartbill.service;

import com.smartbill.dto.InvoiceCreateDto;
import com.smartbill.dto.InvoiceDto;
import com.smartbill.dto.InvoiceItemCreateDto;
import com.smartbill.entity.Invoice;
import com.smartbill.entity.InvoiceItem;
import com.smartbill.entity.Product;
import com.smartbill.entity.User;
import com.smartbill.mapper.InvoiceMapper;
import com.smartbill.repository.InvoiceRepository;
import com.smartbill.repository.ProductRepository;
import com.smartbill.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final InvoiceMapper invoiceMapper;
    private final SecurityUtils securityUtils;

    public InvoiceService(InvoiceRepository invoiceRepository, ProductRepository productRepository, InvoiceMapper invoiceMapper, SecurityUtils securityUtils) {
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
        this.invoiceMapper = invoiceMapper;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public InvoiceDto createInvoice(InvoiceCreateDto createDto) {
        User currentUser = securityUtils.getCurrentUser();
        Invoice invoice = new Invoice();
        invoice.setUser(currentUser);
        invoice.setCustomerName(createDto.getCustomerName());
        invoice.setCustomerMobile(createDto.getCustomerMobile());
        invoice.setPaymentMethod(createDto.getPaymentMethod());
        invoice.setDate(LocalDateTime.now());
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        invoice.setInvoiceNumber("INV-" + timestamp);

        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;

        List<InvoiceItem> items = new ArrayList<>();

        for (InvoiceItemCreateDto itemDto : createDto.getItems()) {
            Product product = productRepository.findByIdAndUser(itemDto.getProductId(), currentUser)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDto.getProductId()));

            if (product.getStock() < itemDto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName() + ". Available: " + product.getStock());
            }

            // Reduce stock
            product.setStock(product.getStock() - itemDto.getQuantity());
            productRepository.save(product);

            InvoiceItem item = new InvoiceItem();
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setGstPercentage(product.getGstPercentage());

            BigDecimal itemTotalExGst = product.getPrice().multiply(new BigDecimal(itemDto.getQuantity()));
            BigDecimal itemGst = itemTotalExGst.multiply(new BigDecimal(product.getGstPercentage())).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            BigDecimal itemTotalAmount = itemTotalExGst.add(itemGst);

            item.setGstAmount(itemGst);
            item.setTotalAmount(itemTotalAmount);

            subTotal = subTotal.add(itemTotalExGst);
            totalGst = totalGst.add(itemGst);

            items.add(item);
        }

        invoice.setSubTotal(subTotal);
        invoice.setTotalGst(totalGst);
        invoice.setGrandTotal(subTotal.add(totalGst));
        invoice.setItems(items);

        Invoice saved = invoiceRepository.save(invoice);
        
        saved.setInvoiceNumber("INV-" + String.format("%06d", saved.getId()));
        saved = invoiceRepository.save(saved);

        return invoiceMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<InvoiceDto> getAllInvoices(String search) {
        User currentUser = securityUtils.getCurrentUser();
        List<Invoice> invoices;
        if (search != null && !search.trim().isEmpty()) {
            invoices = invoiceRepository.searchByUser(currentUser, search.trim());
        } else {
            invoices = invoiceRepository.findByUserOrderByDateDesc(currentUser);
        }
        return invoices.stream()
                .map(invoiceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceById(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Invoice invoice = invoiceRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        return invoiceMapper.toDto(invoice);
    }

    @Transactional
    public void deleteInvoice(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Invoice invoice = invoiceRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        // Restore product stock
        for (InvoiceItem item : invoice.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }
        
        invoiceRepository.delete(invoice);
    }
}
