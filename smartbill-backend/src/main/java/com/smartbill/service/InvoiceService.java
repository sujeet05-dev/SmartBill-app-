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
            
            item.setUnitPrice(itemPrice.setScale(4, RoundingMode.HALF_UP));
            
            // If it's a non-GST bill, force GST to 0 regardless of product settings
            if (!isGstBill) {
                itemGstPct = 0.0;
            }
            item.setGstPercentage(itemGstPct);

            BigDecimal itemTotalExGst = itemPrice.multiply(new BigDecimal(itemDto.getQuantity())).setScale(4, RoundingMode.HALF_UP);
            BigDecimal itemGst = itemTotalExGst.multiply(new BigDecimal(itemGstPct)).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
            BigDecimal itemTotalAmount = itemTotalExGst.add(itemGst).setScale(4, RoundingMode.HALF_UP);

            item.setGstAmount(itemGst);
            item.setTotalAmount(itemTotalAmount);

            subTotal = subTotal.add(itemTotalExGst);
            totalGst = totalGst.add(itemGst);

            items.add(item);
        }

        // CGST and SGST split (each = total GST / 2)
        BigDecimal cgst = totalGst.divide(new BigDecimal(2), 4, RoundingMode.HALF_UP);
        BigDecimal sgst = totalGst.subtract(cgst).setScale(4, RoundingMode.HALF_UP);

        BigDecimal roundOff = createDto.getRoundOff() != null 
                ? createDto.getRoundOff().setScale(4, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        invoice.setRoundOff(roundOff);

        invoice.setSubTotal(subTotal.setScale(4, RoundingMode.HALF_UP));
        invoice.setTotalGst(totalGst.setScale(4, RoundingMode.HALF_UP));
        invoice.setCgstAmount(cgst);
        invoice.setSgstAmount(sgst);
        invoice.setGrandTotal(subTotal.add(totalGst).add(roundOff).setScale(4, RoundingMode.HALF_UP));

        // Amount in words
        invoice.setAmountInWords(NumberToWordsConverter.convert(invoice.getGrandTotal()));

        invoice.setItems(items);

        // Determine sequential invoice number before saving in a single INSERT
        long invoiceNum;
        if (isGstBill) {
            Optional<String> maxOpt = invoiceRepository.findMaxGstInvoiceNumberByUser(currentUser.getId());
            long max = 499;
            if (maxOpt.isPresent()) {
                try {
                    long val = Long.parseLong(maxOpt.get());
                    if (val > max) max = val;
                } catch (NumberFormatException ignored) {}
            }
            invoiceNum = max + 1;
            invoice.setInvoiceNumber(String.format("%06d", invoiceNum));
        } else {
            Optional<String> maxOpt = invoiceRepository.findMaxNonGstInvoiceNumberByUser(currentUser.getId());
            long max = 0;
            if (maxOpt.isPresent()) {
                try {
                    String numStr = maxOpt.get().replace("EST-", "");
                    long val = Long.parseLong(numStr);
                    if (val > max) max = val;
                } catch (NumberFormatException ignored) {}
            }
            invoiceNum = max + 1;
            invoice.setInvoiceNumber("EST-" + String.format("%04d", invoiceNum));
        }

        Invoice saved = invoiceRepository.save(invoice);
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
        
        // Explicitly clear nested element collections to prevent Hibernate ConstraintViolationException
        // when the database lacks ON DELETE CASCADE for element collections.
        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                if (item.getSelectedImeis() != null) {
                    item.getSelectedImeis().clear();
                }
            }
            invoice.getItems().clear();
        }
        
        // Flush the changes to ensure child records (and element collections) are deleted before the parent invoice
        invoiceRepository.saveAndFlush(invoice);
        
        invoiceRepository.delete(invoice);
    }

    @Transactional(readOnly = true)
    public List<MonthlySummaryDto> getMonthlySummary() {
        User currentUser = securityUtils.getCurrentUser();
        List<Object[]> rows = invoiceRepository.getMonthlySummaryRaw(currentUser.getId());
        List<MonthlySummaryDto> result = new ArrayList<>(rows.size());

        for (Object[] row : rows) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            long count = ((Number) row[2]).longValue();
            BigDecimal amount = new BigDecimal(row[3].toString());
            BigDecimal gst = new BigDecimal(row[4].toString());

            String monthName = java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
            String monthYear = monthName + " " + year;
            result.add(new MonthlySummaryDto(monthYear, year, month, count, amount, gst));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<InvoiceDto> getInvoicesByMonth(int year, int month) {
        User currentUser = securityUtils.getCurrentUser();
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDateTime start = firstDay.atStartOfDay();
        LocalDateTime end = firstDay.plusMonths(1).atStartOfDay();

        List<Invoice> invoices = invoiceRepository.findByMonthAndUser(currentUser, start, end);
        return invoices.stream()
                .map(invoiceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        User currentUser = securityUtils.getCurrentUser();
        BigDecimal totalRevenue = invoiceRepository.getTotalRevenueByUser(currentUser);
        Long totalItemsSold = invoiceRepository.getTotalItemsSoldByUser(currentUser);
        return new DashboardStatsDto(totalRevenue, totalItemsSold);
    }
}
