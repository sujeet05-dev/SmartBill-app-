package com.smartbill.controller;

import com.smartbill.dto.InvoiceCreateDto;
import com.smartbill.dto.InvoiceDto;
import com.smartbill.service.InvoiceService;
import com.smartbill.service.PdfGeneratorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PdfGeneratorService pdfGeneratorService;

    public InvoiceController(InvoiceService invoiceService, PdfGeneratorService pdfGeneratorService) {
        this.invoiceService = invoiceService;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    @PostMapping
    public ResponseEntity<InvoiceDto> createInvoice(@Valid @RequestBody InvoiceCreateDto createDto) {
        return new ResponseEntity<>(invoiceService.createInvoice(createDto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceDto>> getAllInvoices(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.getInvoiceById(id);
        byte[] pdfBytes = pdfGeneratorService.generateInvoicePdf(invoice);
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"invoice_" + invoice.getInvoiceNumber() + ".pdf\"")
                .body(pdfBytes);
    }
}
