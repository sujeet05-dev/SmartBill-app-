package com.smartbill.dto;

import java.math.BigDecimal;
import java.util.List;

public class InvoiceItemDto {
    private Long id;
    private ProductDto product;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Double gstPercentage;
    private BigDecimal gstAmount;
    private BigDecimal totalAmount;
    private List<String> selectedImeis;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ProductDto getProduct() { return product; }
    public void setProduct(ProductDto product) { this.product = product; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public Double getGstPercentage() { return gstPercentage; }
    public void setGstPercentage(Double gstPercentage) { this.gstPercentage = gstPercentage; }
    
    public BigDecimal getGstAmount() { return gstAmount; }
    public void setGstAmount(BigDecimal gstAmount) { this.gstAmount = gstAmount; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public List<String> getSelectedImeis() { return selectedImeis; }
    public void setSelectedImeis(List<String> selectedImeis) { this.selectedImeis = selectedImeis; }
}
