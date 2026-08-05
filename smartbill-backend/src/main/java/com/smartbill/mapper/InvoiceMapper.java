package com.smartbill.mapper;

import com.smartbill.dto.InvoiceDto;
import com.smartbill.dto.InvoiceItemDto;
import com.smartbill.entity.Invoice;
import com.smartbill.entity.InvoiceItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class InvoiceMapper {

    private final ProductMapper productMapper;

    public InvoiceMapper(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public InvoiceDto toDto(Invoice invoice) {
        if (invoice == null) return null;
        InvoiceDto dto = new InvoiceDto();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setDate(invoice.getDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setCustomerName(invoice.getCustomerName());
        dto.setCustomerMobile(invoice.getCustomerMobile());
        dto.setPlaceOfSupply(invoice.getPlaceOfSupply());
        dto.setPaymentMethod(invoice.getPaymentMethod());
        dto.setSubTotal(invoice.getSubTotal());
        dto.setTotalGst(invoice.getTotalGst());
        dto.setCgstAmount(invoice.getCgstAmount());
        dto.setSgstAmount(invoice.getSgstAmount());
        dto.setGrandTotal(invoice.getGrandTotal());
        dto.setReceivedAmount(invoice.getReceivedAmount());
        dto.setAmountInWords(invoice.getAmountInWords());

        if (invoice.getItems() != null) {
            dto.setItems(invoice.getItems().stream().map(this::toItemDto).collect(Collectors.toList()));
        }

        return dto;
    }

    private InvoiceItemDto toItemDto(InvoiceItem item) {
        if (item == null) return null;
        InvoiceItemDto dto = new InvoiceItemDto();
        dto.setId(item.getId());
        dto.setProduct(productMapper.toDto(item.getProduct()));
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setGstPercentage(item.getGstPercentage());
        dto.setGstAmount(item.getGstAmount());
        dto.setTotalAmount(item.getTotalAmount());
        dto.setSelectedImeis(item.getSelectedImeis() != null ? new java.util.ArrayList<>(item.getSelectedImeis()) : null);
        dto.setSelectedHsnCodes(item.getSelectedHsnCodes() != null ? new java.util.ArrayList<>(item.getSelectedHsnCodes()) : null);
        return dto;
    }
}
