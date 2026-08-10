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
        invoice.setInvoiceNumber("TEMP-" + timestamp);

        // Set received amount
        invoice.setReceivedAmount(createDto.getReceivedAmount() != null ? createDto.getReceivedAmount() : BigDecimal.ZERO);

        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;

        List<InvoiceItem> items = new ArrayList<>();

        Boolean isGstBill = createDto.getIsGst() != null ? createDto.getIsGst() : true;
        invoice.setIsGst(isGstBill);

        for (InvoiceItemCreateDto itemDto : createDto.getItems()) {
            InvoiceItem item = new InvoiceItem();
            item.setQuantity(itemDto.getQuantity());

            BigDecimal itemPrice;
            double itemGstPct;
            
            if (itemDto.getProductId() != null && itemDto.getProductId() > 0) {
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
                
                item.setProduct(product);
                item.setProductName(product.getName());
                
                if (!isGstBill && itemDto.getUnitPrice() != null) {
                    itemPrice = itemDto.getUnitPrice();
                } else {
                    itemPrice = product.getPrice();
                }
                itemGstPct = product.getGstPercentage();
                
                if (selectedImeis != null && !selectedImeis.isEmpty()) {
                    item.setSelectedImeis(new ArrayList<>(selectedImeis));
                }
            } else {
                Product dummyProduct = productRepository.findBySkuAndUser("MANUAL_ENTRY_DUMMY", currentUser).orElse(null);
                if (dummyProduct == null) {
                    dummyProduct = new Product();
                    dummyProduct.setName("Manual Entry Item");
                    dummyProduct.setBrand("-");
                    dummyProduct.setCategory("-");
                    dummyProduct.setPrice(BigDecimal.ZERO);
                    dummyProduct.setGstPercentage(0.0);
                    dummyProduct.setStock(999999);
                    dummyProduct.setSku("MANUAL_ENTRY_DUMMY");
                    dummyProduct.setUser(currentUser);
                    dummyProduct = productRepository.save(dummyProduct);
                }
                
                item.setProduct(dummyProduct);
                item.setProductName(itemDto.getProductName());
                itemPrice = itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : BigDecimal.ZERO;
                itemGstPct = 0.0;
            }
            
            item.setUnitPrice(itemPrice);
            
            // If it's a non-GST bill, force GST to 0 regardless of product settings
            if (!isGstBill) {
                itemGstPct = 0.0;
            }
            item.setGstPercentage(itemGstPct);

            BigDecimal itemTotalExGst = itemPrice.multiply(new BigDecimal(itemDto.getQuantity()));
            BigDecimal itemGst = itemTotalExGst.multiply(new BigDecimal(itemGstPct)).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
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
        Invoice lastInvoice;
        if (isGstBill) {
            lastInvoice = invoiceRepository.findLastGstInvoice(currentUser.getId(), saved.getId());
        } else {
            lastInvoice = invoiceRepository.findLastNonGstInvoice(currentUser.getId(), saved.getId());
        }
        if (isGstBill) {
            long invoiceNum = 500;
            if (lastInvoice != null && lastInvoice.getInvoiceNumber() != null) {
                try {
                    invoiceNum = Long.parseLong(lastInvoice.getInvoiceNumber()) + 1;
                } catch (NumberFormatException e) {
                    // fallback
                    invoiceNum = 500 + saved.getId();
                }
            }
            saved.setInvoiceNumber(String.format("%06d", invoiceNum));
        } else {
            long invoiceNum = 1;
            if (lastInvoice != null && lastInvoice.getInvoiceNumber() != null) {
                try {
                    String numStr = lastInvoice.getInvoiceNumber().replace("EST-", "");
                    invoiceNum = Long.parseLong(numStr) + 1;
                } catch (NumberFormatException e) {
                    // fallback
                    invoiceNum = saved.getId();
                }
            }
            saved.setInvoiceNumber("EST-" + String.format("%04d", invoiceNum));
        }
        saved = invoiceRepository.save(saved);

        return invoiceMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<InvoiceDto> getAllInvoices(String search, Boolean isGst) {
        User currentUser = securityUtils.getCurrentUser();
        List<Invoice> invoices;
        if (search != null && !search.trim().isEmpty()) {
            invoices = invoiceRepository.searchByUserAndIsGst(currentUser, search.trim(), isGst);
        } else {
            invoices = invoiceRepository.findByUserAndIsGstOrderByDateDesc(currentUser, isGst);
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
        
        // Do not restore product stock as per user request
        
        invoiceRepository.delete(invoice);
    }

    @Transactional(readOnly = true)
    public List<MonthlySummaryDto> getMonthlySummary() {
        User currentUser = securityUtils.getCurrentUser();
        List<Invoice> invoices = invoiceRepository.findByUserAndIsGstOrderByDateDesc(currentUser, true);

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
        List<Invoice> allInvoices = invoiceRepository.findByUserAndIsGstOrderByDateDesc(currentUser, true);

        return allInvoices.stream()
                .filter(i -> i.getDate().getYear() == year && i.getDate().getMonthValue() == month)
                .map(invoiceMapper::toDto)
                .collect(Collectors.toList());
    }
}
