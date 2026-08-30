package com.smartbill.controller;

import com.smartbill.dto.InvoiceCreateDto;
import com.smartbill.dto.InvoiceDto;
import com.smartbill.dto.MonthlySummaryDto;
import com.smartbill.dto.DashboardStatsDto;
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
    public ResponseEntity<List<InvoiceDto>> getAllInvoices(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "true") Boolean isGst) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(search, isGst));
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<List<MonthlySummaryDto>> getMonthlySummary() {
        return ResponseEntity.ok(invoiceService.getMonthlySummary());
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(invoiceService.getDashboardStats());
    }

    @GetMapping("/by-month")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByMonth(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(invoiceService.getInvoicesByMonth(year, month));
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

    @GetMapping(value = "/monthly-report/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getMonthlyReportPdf(@RequestParam int year, @RequestParam int month) {
        List<InvoiceDto> invoices = invoiceService.getInvoicesByMonth(year, month);
        
        // If there are no invoices, we can still generate an empty report or handle it.
        // The frontend will usually block this call if there are 0 invoices, 
        // but it's safe to generate one with total 0 anyway.
        byte[] pdfBytes = pdfGeneratorService.generateMonthlyReportPdf(year, month, invoices);
        
        String monthStr = String.format("%02d", month);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"monthly_report_" + year + "_" + monthStr + ".pdf\"")
                .body(pdfBytes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
}
