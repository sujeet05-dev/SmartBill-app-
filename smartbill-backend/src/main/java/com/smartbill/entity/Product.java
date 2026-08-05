package com.smartbill.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Double gstPercentage;

    @Column(nullable = false)
    private Integer stock;

    @Column
    private String sku;

    @ElementCollection
    @CollectionTable(name = "product_imeis", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "imei")
    private List<String> availableImeis = new ArrayList<>();

    @Column
    private String unit;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_hsn_codes", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "hsn_code")
    private List<String> availableHsnCodes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Product() {
        this.unit = "PCS";
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Double getGstPercentage() { return gstPercentage; }
    public void setGstPercentage(Double gstPercentage) { this.gstPercentage = gstPercentage; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public List<String> getAvailableImeis() { return availableImeis; }
    public void setAvailableImeis(List<String> availableImeis) { this.availableImeis = availableImeis; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public List<String> getAvailableHsnCodes() { return availableHsnCodes; }
    public void setAvailableHsnCodes(List<String> availableHsnCodes) { this.availableHsnCodes = availableHsnCodes; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
