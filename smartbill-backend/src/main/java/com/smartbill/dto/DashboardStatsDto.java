package com.smartbill.dto;

import java.math.BigDecimal;

public class DashboardStatsDto {
    private BigDecimal totalRevenue;
    private Long totalItemsSold;

    public DashboardStatsDto() {
    }

    public DashboardStatsDto(BigDecimal totalRevenue, Long totalItemsSold) {
        this.totalRevenue = totalRevenue;
        this.totalItemsSold = totalItemsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalItemsSold() {
        return totalItemsSold;
    }

    public void setTotalItemsSold(Long totalItemsSold) {
        this.totalItemsSold = totalItemsSold;
    }
}
