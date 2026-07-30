package com.smartbill.dto;

import com.smartbill.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class InvoiceCreateDto {

    private String customerName;
    private String customerMobile;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotEmpty(message = "At least one product must be added")
    @Valid
    private List<InvoiceItemCreateDto> items;

    public InvoiceCreateDto() {}

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerMobile() { return customerMobile; }
    public void setCustomerMobile(String customerMobile) { this.customerMobile = customerMobile; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<InvoiceItemCreateDto> getItems() { return items; }
    public void setItems(List<InvoiceItemCreateDto> items) { this.items = items; }
}
