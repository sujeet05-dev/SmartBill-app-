package com.smartbill.service;

import com.smartbill.dto.*;
import com.smartbill.entity.*;
import com.smartbill.mapper.InvoiceMapper;
import com.smartbill.repository.InvoiceRepository;
import com.smartbill.repository.ProductRepository;
import com.smartbill.security.SecurityUtils;
import com.smartbill.util.NumberToWordsConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final InvoiceMapper invoiceMapper;
    private final SecurityUtils securityUtils;
    private final ShopService shopService;

    public InvoiceService(InvoiceRepository invoiceRepository, ProductRepository productRepository, InvoiceMapper invoiceMapper, SecurityUtils securityUtils, ShopService shopService) {
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
        this.invoiceMapper = invoiceMapper;
        this.securityUtils = securityUtils;
        this.shopService = shopService;
    }

    @Transactional
    public InvoiceDto createInvoice(InvoiceCreateDto createDto) {
        User currentUser = securityUtils.getCurrentUser();
        Invoice invoice = new Invoice();
        invoice.setUser(currentUser);
        invoice.setCustomerName(createDto.getCustomerName());
        invoice.setCustomerMobile(createDto.getCustomerMobile());
        invoice.setCustomerAddress(createDto.getCustomerAddress());
        invoice.setPaymentMethod(createDto.getPaymentMethod());
        
        LocalDateTime now = LocalDateTime.now();
        invoice.setDate(now);
        invoice.setDueDate(now.toLocalDate().plusDays(7));

        // Auto-fill place of supply from shop state
        ShopDto shop = shopService.getShop();
        if (shop != null && shop.getState() != null) {
            invoice.setPlaceOfSupply(shop.getState());
        }
        
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        invoice.setInvoiceNumber("INV-" + timestamp);

        // Set received amount
        invoice.setReceivedAmount(createDto.getReceivedAmount() != null ? createDto.getReceivedAmount() : BigDecimal.ZERO);

        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;

        List<InvoiceItem> items = new ArrayList<>();

        for (InvoiceItemCreateDto itemDto : createDto.getItems()) {
            Product product = productRepository.findByIdAndUser(itemDto.getProductId(), currentUser)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDto.getProductId()));

            if (product.getStock() < itemDto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName() + ". Available: " + product.getStock());
            }

            // Validate and handle IMEIs
            List<String> selectedImeis = itemDto.getSelectedImeis();
            if (selectedImeis != null && !selectedImeis.isEmpty()) {
                if (selectedImeis.size() != itemDto.getQuantity()) {
                    throw new RuntimeException("Number of selected IMEIs must match the quantity for product: " + product.getName());
                }
                for (String imei : selectedImeis) {
                    if (!product.getAvailableImeis().contains(imei)) {
                        throw new RuntimeException("IMEI " + imei + " is not available for product: " + product.getName());
                    }
                }
                product.getAvailableImeis().removeAll(selectedImeis);
            }

            // Reduce stock
            product.setStock(product.getStock() - itemDto.getQuantity());
            productRepository.save(product);

            InvoiceItem item = new InvoiceItem();
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setGstPercentage(product.getGstPercentage());
            if (selectedImeis != null && !selectedImeis.isEmpty()) {
                item.setSelectedImeis(new ArrayList<>(selectedImeis));
            }

            BigDecimal itemTotalExGst = product.getPrice().multiply(new BigDecimal(itemDto.getQuantity()));
            BigDecimal itemGst = itemTotalExGst.multiply(new BigDecimal(product.getGstPercentage())).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            BigDecimal itemTotalAmount = itemTotalExGst.add(itemGst);

            item.setGstAmount(itemGst);
            item.setTotalAmount(itemTotalAmount);

            subTotal = subTotal.add(itemTotalExGst);
            totalGst = totalGst.add(itemGst);

            items.add(item);
        }

        // CGST and SGST split (each = total GST / 2)
        BigDecimal cgst = totalGst.divide(new BigDecimal(2), 2, RoundingMode.HALF_UP);
        BigDecimal sgst = totalGst.subtract(cgst);

        invoice.setSubTotal(subTotal);
        invoice.setTotalGst(totalGst);
        invoice.setCgstAmount(cgst);
        invoice.setSgstAmount(sgst);
        invoice.setGrandTotal(subTotal.add(totalGst));

        // Amount in words
        invoice.setAmountInWords(NumberToWordsConverter.convert(invoice.getGrandTotal()));

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
                if (item.getSelectedImeis() != null && !item.getSelectedImeis().isEmpty()) {
                    product.getAvailableImeis().addAll(item.getSelectedImeis());
                }
                productRepository.save(product);
            }
        }
        
        invoiceRepository.delete(invoice);
    }

    @Transactional(readOnly = true)
    public List<MonthlySummaryDto> getMonthlySummary() {
        User currentUser = securityUtils.getCurrentUser();
        List<Invoice> invoices = invoiceRepository.findByUserOrderByDateDesc(currentUser);

        Map<String, MonthlySummaryDto> summaryMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");

        for (Invoice invoice : invoices) {
            String monthYear = invoice.getDate().format(formatter);
            int year = invoice.getDate().getYear();
            int month = invoice.getDate().getMonthValue();

            MonthlySummaryDto summary = summaryMap.computeIfAbsent(monthYear, k ->
                    new MonthlySummaryDto(k, year, month, 0, BigDecimal.ZERO, BigDecimal.ZERO)
            );

            summary.setTotalInvoices(summary.getTotalInvoices() + 1);
            summary.setTotalAmount(summary.getTotalAmount().add(invoice.getGrandTotal()));
            summary.setTotalGst(summary.getTotalGst().add(invoice.getTotalGst()));
        }

        return new ArrayList<>(summaryMap.values());
    }

    @Transactional(readOnly = true)
    public List<InvoiceDto> getInvoicesByMonth(int year, int month) {
        User currentUser = securityUtils.getCurrentUser();
        List<Invoice> invoices = invoiceRepository.findByUserOrderByDateDesc(currentUser);

        return invoices.stream()
                .filter(i -> i.getDate().getYear() == year && i.getDate().getMonthValue() == month)
                .map(invoiceMapper::toDto)
                .collect(Collectors.toList());
    }
}
