package com.smartbill.dto;

import java.math.BigDecimal;

public class MonthlySummaryDto {
    private String monthYear;
    private int year;
    private int month;
    private long totalInvoices;
    private BigDecimal totalAmount;
    private BigDecimal totalGst;

    public MonthlySummaryDto() {}

    public MonthlySummaryDto(String monthYear, int year, int month, long totalInvoices, BigDecimal totalAmount, BigDecimal totalGst) {
        this.monthYear = monthYear;
        this.year = year;
        this.month = month;
        this.totalInvoices = totalInvoices;
        this.totalAmount = totalAmount;
        this.totalGst = totalGst;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public long getTotalInvoices() {
        return totalInvoices;
    }

    public void setTotalInvoices(long totalInvoices) {
        this.totalInvoices = totalInvoices;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalGst() {
        return totalGst;
    }

    public void setTotalGst(BigDecimal totalGst) {
        this.totalGst = totalGst;
    }
}
