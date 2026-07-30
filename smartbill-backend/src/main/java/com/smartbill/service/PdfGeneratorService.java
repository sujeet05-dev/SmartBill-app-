package com.smartbill.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smartbill.dto.InvoiceDto;
import com.smartbill.dto.InvoiceItemDto;
import com.smartbill.dto.ShopDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    private final ShopService shopService;

    public PdfGeneratorService(ShopService shopService) {
        this.shopService = shopService;
    }

    public byte[] generateInvoicePdf(InvoiceDto invoice) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        // 1. Initialize Document
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Setup fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            
            // 2. Fetch Shop Details
            ShopDto shop = shopService.getShop();

            // 3. Header Section (Shop Info & Invoice Title)
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1, 1});

            // Left Side: Shop Info
            PdfPCell shopCell = new PdfPCell();
            shopCell.setBorder(Rectangle.NO_BORDER);
            if (shop != null) {
                shopCell.addElement(new Paragraph(shop.getName(), titleFont));
                shopCell.addElement(new Paragraph(shop.getAddress(), normalFont));
                shopCell.addElement(new Paragraph("Phone: " + shop.getPhone(), normalFont));
                if (shop.getEmail() != null && !shop.getEmail().isEmpty()) {
                    shopCell.addElement(new Paragraph("Email: " + shop.getEmail(), normalFont));
                }
                shopCell.addElement(new Paragraph("GSTIN: " + shop.getGstin(), boldFont));
            } else {
                shopCell.addElement(new Paragraph("SmartBill Electronics", titleFont));
            }
            headerTable.addCell(shopCell);

            // Right Side: Tax Invoice text
            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph taxInvoice = new Paragraph("TAX INVOICE", titleFont);
            taxInvoice.setAlignment(Element.ALIGN_RIGHT);
            titleCell.addElement(taxInvoice);
            headerTable.addCell(titleCell);
            
            document.add(headerTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // 4. Invoice & Customer Details
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);

            PdfPCell billToCell = new PdfPCell();
            billToCell.setBorder(Rectangle.NO_BORDER);
            billToCell.addElement(new Paragraph("Bill To:", boldFont));
            if (invoice.getCustomerName() != null && !invoice.getCustomerName().isEmpty()) {
                billToCell.addElement(new Paragraph(invoice.getCustomerName(), normalFont));
            } else {
                billToCell.addElement(new Paragraph("Cash Customer", normalFont));
            }
            if (invoice.getCustomerMobile() != null && !invoice.getCustomerMobile().isEmpty()) {
                billToCell.addElement(new Paragraph("Phone: " + invoice.getCustomerMobile(), normalFont));
            }
            infoTable.addCell(billToCell);

            PdfPCell invoiceDetailsCell = new PdfPCell();
            invoiceDetailsCell.setBorder(Rectangle.NO_BORDER);
            invoiceDetailsCell.addElement(new Paragraph("Invoice No: " + invoice.getInvoiceNumber(), boldFont));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
            invoiceDetailsCell.addElement(new Paragraph("Date: " + invoice.getDate().format(formatter), normalFont));
            invoiceDetailsCell.addElement(new Paragraph("Payment Method: " + invoice.getPaymentMethod().name(), normalFont));
            infoTable.addCell(invoiceDetailsCell);
            
            document.add(infoTable);
            document.add(new Paragraph(" "));

            // 5. Product Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1, 1.5f, 1, 1.5f, 2}); // Relative widths

            // Table Header
            String[] headers = {"Description", "Qty", "Rate (₹)", "GST %", "GST (₹)", "Amount (₹)"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                cell.setPadding(6);
                table.addCell(cell);
            }

            // Table Rows
            for (InvoiceItemDto item : invoice.getItems()) {
                PdfPCell nameCell = new PdfPCell(new Phrase(item.getProduct().getName(), normalFont));
                nameCell.setPadding(5);
                table.addCell(nameCell);

                PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                qtyCell.setPadding(5);
                table.addCell(qtyCell);

                PdfPCell rateCell = new PdfPCell(new Phrase(item.getUnitPrice().toString(), normalFont));
                rateCell.setPadding(5);
                table.addCell(rateCell);

                PdfPCell gstPctCell = new PdfPCell(new Phrase(String.valueOf(item.getGstPercentage()) + "%", normalFont));
                gstPctCell.setPadding(5);
                table.addCell(gstPctCell);

                PdfPCell gstAmtCell = new PdfPCell(new Phrase(item.getGstAmount().toString(), normalFont));
                gstAmtCell.setPadding(5);
                table.addCell(gstAmtCell);

                PdfPCell totalCell = new PdfPCell(new Phrase(item.getTotalAmount().toString(), normalFont));
                totalCell.setPadding(5);
                table.addCell(totalCell);
            }

            document.add(table);

            // 6. Summary Section
            document.add(new Paragraph(" "));
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(40);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            addSummaryRow(summaryTable, "Subtotal:", invoice.getSubTotal().toString(), normalFont);
            addSummaryRow(summaryTable, "Total GST:", invoice.getTotalGst().toString(), normalFont);
            addSummaryRow(summaryTable, "Grand Total (₹):", invoice.getGrandTotal().toString(), boldFont);

            document.add(summaryTable);
            
            // 7. Footer Section
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            
            PdfPCell termsCell = new PdfPCell();
            termsCell.setBorder(Rectangle.NO_BORDER);
            termsCell.addElement(new Paragraph("Terms & Conditions:", boldFont));
            termsCell.addElement(new Paragraph("1. Goods once sold will not be taken back.", normalFont));
            termsCell.addElement(new Paragraph("2. Thank you for your business!", normalFont));
            footerTable.addCell(termsCell);
            
            PdfPCell signCell = new PdfPCell();
            signCell.setBorder(Rectangle.NO_BORDER);
            signCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph sign = new Paragraph("Authorised Signatory", boldFont);
            sign.setAlignment(Element.ALIGN_RIGHT);
            signCell.addElement(new Paragraph(" "));
            signCell.addElement(new Paragraph(" "));
            signCell.addElement(sign);
            footerTable.addCell(signCell);
            
            document.add(footerTable);

            document.close();
            
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }

        return out.toByteArray();
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }
}
