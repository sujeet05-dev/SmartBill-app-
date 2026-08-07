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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    private final ShopService shopService;

    public PdfGeneratorService(ShopService shopService) {
        this.shopService = shopService;
    }

    public byte[] generateInvoicePdf(InvoiceDto invoice) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
            Font smallBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font bigBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            ShopDto shop = shopService.getShop();
            String shopName = shop != null ? shop.getName() : "SmartBill Shop";

            // ========================
            // 1. TAX INVOICE header bar
            // ========================
            PdfPTable topBar = new PdfPTable(2);
            topBar.setWidthPercentage(100);
            topBar.setWidths(new float[]{1, 2});

            PdfPCell taxLabel = new PdfPCell(new Phrase("TAX INVOICE", headerFont));
            taxLabel.setBorder(Rectangle.BOTTOM);
            taxLabel.setPadding(6);
            topBar.addCell(taxLabel);

            PdfPCell origLabel = new PdfPCell(new Phrase("ORIGINAL FOR RECIPIENT", smallFont));
            origLabel.setBorder(Rectangle.BOTTOM);
            origLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            origLabel.setPadding(6);
            topBar.addCell(origLabel);

            document.add(topBar);
            document.add(new Paragraph(" "));

            // ========================
            // 2. Shop Info Section
            // ========================
            Paragraph shopTitle = new Paragraph("M/S " + shopName, titleFont);
            document.add(shopTitle);

            if (shop != null) {
                StringBuilder addr = new StringBuilder();
                addr.append(shop.getAddress());
                if (shop.getState() != null && !shop.getState().isEmpty()) {
                    addr.append(", ").append(shop.getState());
                }
                if (shop.getPincode() != null && !shop.getPincode().isEmpty()) {
                    addr.append(", ").append(shop.getPincode());
                }
                document.add(new Paragraph(addr.toString(), normalFont));
                document.add(new Paragraph("Mobile: " + shop.getPhone() + "      GSTIN: " + shop.getGstin(), boldFont));
                if (shop.getEmail() != null && !shop.getEmail().isEmpty()) {
                    document.add(new Paragraph("Email: " + shop.getEmail(), normalFont));
                }
            }

            document.add(new Paragraph(" "));

            // ========================
            // 3. Invoice Info Row
            // ========================
            PdfPTable infoRow = new PdfPTable(3);
            infoRow.setWidthPercentage(100);
            infoRow.setWidths(new float[]{1, 1.5f, 1});

            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            PdfPCell invNoCell = createCell("Invoice No.: " + invoice.getInvoiceNumber().replace("INV-", ""), boldFont);
            invNoCell.setBackgroundColor(new java.awt.Color(240, 240, 240));
            invNoCell.setBorder(Rectangle.BOX);
            infoRow.addCell(invNoCell);

            PdfPCell invDateCell = createCell("Invoice Date: " + invoice.getDate().format(dateFormat), boldFont);
            invDateCell.setBackgroundColor(new java.awt.Color(240, 240, 240));
            invDateCell.setBorder(Rectangle.BOX);
            infoRow.addCell(invDateCell);

            String dueDateStr = invoice.getDueDate() != null ? invoice.getDueDate().format(dateFormat) : "N/A";
            PdfPCell dueDateCell = createCell("Due Date: " + dueDateStr, boldFont);
            dueDateCell.setBackgroundColor(new java.awt.Color(240, 240, 240));
            dueDateCell.setBorder(Rectangle.BOX);
            dueDateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            infoRow.addCell(dueDateCell);

            document.add(infoRow);
            document.add(new Paragraph(" "));

            // ========================
            // 4. BILL TO / SHIP TO
            // ========================
            PdfPTable billShipTable = new PdfPTable(2);
            billShipTable.setWidthPercentage(100);

            PdfPCell billToCell = new PdfPCell();
            billToCell.setBorder(Rectangle.NO_BORDER);
            billToCell.addElement(new Paragraph("BILL TO", labelFont));
            String custName = (invoice.getCustomerName() != null && !invoice.getCustomerName().isEmpty()) ? invoice.getCustomerName() : "Cash Customer";
            billToCell.addElement(new Paragraph(custName, normalFont));
            if (invoice.getCustomerAddress() != null && !invoice.getCustomerAddress().isEmpty()) {
                billToCell.addElement(new Paragraph(invoice.getCustomerAddress(), smallFont));
            }
            if (invoice.getPlaceOfSupply() != null && !invoice.getPlaceOfSupply().isEmpty()) {
                billToCell.addElement(new Paragraph("Place of Supply: " + invoice.getPlaceOfSupply(), smallFont));
            }
            billShipTable.addCell(billToCell);

            PdfPCell shipToCell = new PdfPCell();
            shipToCell.setBorder(Rectangle.NO_BORDER);
            shipToCell.addElement(new Paragraph("SHIP TO", labelFont));
            shipToCell.addElement(new Paragraph(custName, normalFont));
            if (invoice.getCustomerAddress() != null && !invoice.getCustomerAddress().isEmpty()) {
                shipToCell.addElement(new Paragraph(invoice.getCustomerAddress(), smallFont));
            }
            billShipTable.addCell(shipToCell);

            document.add(billShipTable);
            document.add(new Paragraph(" "));

            // ========================
            // 5. Items Table
            // ========================
            PdfPTable itemsTable = new PdfPTable(6);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{3f, 1f, 1f, 1.5f, 1.5f, 1.5f});

            // Header row
            String[] headers = {"ITEMS", "QTY.", "HSN", "RATE", "TAX", "AMOUNT"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
                cell.setPadding(6);
                cell.setBorder(Rectangle.BOX);
                itemsTable.addCell(cell);
            }

            int totalQty = 0;

            for (InvoiceItemDto item : invoice.getItems()) {
                // ITEMS column (name + description + IMEI)
                PdfPCell nameCell = new PdfPCell();
                nameCell.setPadding(5);
                nameCell.setBorder(Rectangle.BOX);
                
                String productName = item.getProduct().getName();
                if (item.getProduct().getDescription() != null && !item.getProduct().getDescription().isEmpty()) {
                    productName += " (" + item.getProduct().getDescription() + ")";
                }
                nameCell.addElement(new Paragraph(productName, boldFont));
                
                if (item.getSelectedImeis() != null && !item.getSelectedImeis().isEmpty()) {
                    nameCell.addElement(new Paragraph("IMEI/Serial No: " + String.join(", ", item.getSelectedImeis()), smallFont));
                }
                itemsTable.addCell(nameCell);

                // QTY column
                String unit = (item.getProduct().getUnit() != null && !item.getProduct().getUnit().isEmpty()) ? item.getProduct().getUnit() : "PCS";
                PdfPCell qtyCell = createCell(item.getQuantity() + " " + unit, normalFont);
                qtyCell.setBorder(Rectangle.BOX);
                qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                itemsTable.addCell(qtyCell);

                totalQty += item.getQuantity();

                // HSN column
                String hsn = (item.getProduct().getHsnCode() != null) ? item.getProduct().getHsnCode() : "";
                PdfPCell hsnCell = createCell(hsn, normalFont);
                hsnCell.setBorder(Rectangle.BOX);
                hsnCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                itemsTable.addCell(hsnCell);

                // RATE column (price before tax)
                PdfPCell rateCell = createCell(formatAmount(item.getUnitPrice()), normalFont);
                rateCell.setBorder(Rectangle.BOX);
                rateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                itemsTable.addCell(rateCell);

                // TAX column
                PdfPCell taxCell = new PdfPCell();
                taxCell.setPadding(5);
                taxCell.setBorder(Rectangle.BOX);
                taxCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                taxCell.addElement(createRightParagraph(formatAmount(item.getGstAmount()), normalFont));
                taxCell.addElement(createRightParagraph("(" + item.getGstPercentage().intValue() + "%)", smallFont));
                itemsTable.addCell(taxCell);

                // AMOUNT column
                PdfPCell amountCell = createCell(formatAmount(item.getTotalAmount()), boldFont);
                amountCell.setBorder(Rectangle.BOX);
                amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                itemsTable.addCell(amountCell);
            }

            document.add(itemsTable);

            // ========================
            // 6. SUBTOTAL Row
            // ========================
            PdfPTable subTotalTable = new PdfPTable(6);
            subTotalTable.setWidthPercentage(100);
            subTotalTable.setWidths(new float[]{3f, 1f, 1f, 1.5f, 1.5f, 1.5f});

            PdfPCell stLabel = createCell("SUBTOTAL", headerFont);
            stLabel.setBorder(Rectangle.BOX);
            subTotalTable.addCell(stLabel);

            PdfPCell stQty = createCell(String.valueOf(totalQty), boldFont);
            stQty.setBorder(Rectangle.BOX);
            stQty.setHorizontalAlignment(Element.ALIGN_CENTER);
            subTotalTable.addCell(stQty);

            PdfPCell stEmpty1 = createCell("", normalFont);
            stEmpty1.setBorder(Rectangle.BOX);
            subTotalTable.addCell(stEmpty1);

            PdfPCell stEmpty2 = createCell("", normalFont);
            stEmpty2.setBorder(Rectangle.BOX);
            subTotalTable.addCell(stEmpty2);

            PdfPCell stGst = createCell("\u20B9 " + formatAmount(invoice.getTotalGst()), boldFont);
            stGst.setBorder(Rectangle.BOX);
            stGst.setHorizontalAlignment(Element.ALIGN_RIGHT);
            subTotalTable.addCell(stGst);

            PdfPCell stTotal = createCell("\u20B9 " + formatAmount(invoice.getGrandTotal()), bigBoldFont);
            stTotal.setBorder(Rectangle.BOX);
            stTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            subTotalTable.addCell(stTotal);

            document.add(subTotalTable);
            document.add(new Paragraph(" "));

            // ========================
            // 7. Terms & Tax Summary Section
            // ========================
            PdfPTable bottomTable = new PdfPTable(2);
            bottomTable.setWidthPercentage(100);
            bottomTable.setWidths(new float[]{1.2f, 1});

            // Left: Terms & Conditions
            PdfPCell termsCell = new PdfPCell();
            termsCell.setBorder(Rectangle.NO_BORDER);
            termsCell.addElement(new Paragraph("TERMS AND CONDITIONS", smallBoldFont));
            if (shop != null && shop.getTermsAndConditions() != null && !shop.getTermsAndConditions().isEmpty()) {
                termsCell.addElement(new Paragraph(shop.getTermsAndConditions(), smallFont));
            } else {
                termsCell.addElement(new Paragraph("1. Goods once sold will not be taken back or exchanged", smallFont));
                termsCell.addElement(new Paragraph("2. All disputes are subject to local jurisdiction only", smallFont));
            }
            bottomTable.addCell(termsCell);

            // Right: Tax breakdown
            PdfPCell taxSummaryCell = new PdfPCell();
            taxSummaryCell.setBorder(Rectangle.NO_BORDER);

            // Determine GST rate from first item
            double gstRate = 18;
            if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
                gstRate = invoice.getItems().get(0).getGstPercentage();
            }
            double halfRate = gstRate / 2;

            PdfPTable taxTable = new PdfPTable(2);
            taxTable.setWidthPercentage(100);

            addTaxRow(taxTable, "Taxable Amount", "\u20B9 " + formatAmount(invoice.getSubTotal()), boldFont, normalFont);
            addTaxRow(taxTable, "CGST @" + (int)halfRate + "%", "\u20B9 " + formatAmount(invoice.getCgstAmount()), normalFont, normalFont);
            addTaxRow(taxTable, "SGST @" + (int)halfRate + "%", "\u20B9 " + formatAmount(invoice.getSgstAmount()), normalFont, normalFont);
            addTaxRow(taxTable, "Total Amount", "\u20B9 " + formatAmount(invoice.getGrandTotal()), bigBoldFont, bigBoldFont);
            addTaxRow(taxTable, "Received Amount", "\u20B9 " + formatAmount(invoice.getReceivedAmount() != null ? invoice.getReceivedAmount() : BigDecimal.ZERO), normalFont, normalFont);

            taxSummaryCell.addElement(taxTable);
            bottomTable.addCell(taxSummaryCell);

            document.add(bottomTable);
            document.add(new Paragraph(" "));

            // ========================
            // 8. Amount in Words
            // ========================
            if (invoice.getAmountInWords() != null) {
                Paragraph wordsLabel = new Paragraph("Total Amount (in words)", smallBoldFont);
                wordsLabel.setAlignment(Element.ALIGN_CENTER);
                document.add(wordsLabel);

                Paragraph wordsValue = new Paragraph(invoice.getAmountInWords(), boldFont);
                wordsValue.setAlignment(Element.ALIGN_CENTER);
                document.add(wordsValue);
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // ========================
            // 9. Authorised Signatory
            // ========================
            Paragraph sigLine = new Paragraph("AUTHORISED SIGNATORY FOR", smallBoldFont);
            sigLine.setAlignment(Element.ALIGN_RIGHT);
            document.add(sigLine);

            Paragraph sigShop = new Paragraph("M/S " + shopName, smallBoldFont);
            sigShop.setAlignment(Element.ALIGN_RIGHT);
            document.add(sigShop);

            document.close();
            
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }

        return out.toByteArray();
    }

    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private Paragraph createRightParagraph(String text, Font font) {
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }

    private void addTaxRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(3);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(3);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }
}
