package com.invoiceme.application.invoice;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.LineItem;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Service for generating PDF invoices.
 */
@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepository;
    
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
    private static final Font HEADING_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US);

    /**
     * Generates a PDF document for the given invoice.
     * 
     * @param invoiceId Invoice ID
     * @return PDF as byte array
     * @throws EntityNotFoundException if invoice not found
     */
    @Transactional(readOnly = true)
    public byte[] generatePdf(UUID invoiceId) {
        // Fetch invoice with customer eagerly loaded
        Invoice invoice = invoiceRepository.findByIdWithCustomer(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + invoiceId));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Paragraph title = new Paragraph("INVOICE", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Invoice number and date
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1, 1});
            
            PdfPCell invoiceNumberCell = new PdfPCell(new Phrase("Invoice #: " + formatInvoiceNumber(invoice.getId()), NORMAL_FONT));
            invoiceNumberCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(invoiceNumberCell);
            
            PdfPCell dateCell = new PdfPCell(new Phrase("Date: " + invoice.getCreatedAt().format(DATE_FORMATTER), NORMAL_FONT));
            dateCell.setBorder(Rectangle.NO_BORDER);
            dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            headerTable.addCell(dateCell);
            
            document.add(headerTable);
            document.add(new Paragraph(" ")); // Spacing

            // Bill To section
            Paragraph billToHeading = new Paragraph("Bill To:", HEADING_FONT);
            billToHeading.setSpacingBefore(10);
            document.add(billToHeading);
            
            Paragraph customerInfo = new Paragraph();
            customerInfo.setFont(NORMAL_FONT);
            customerInfo.add(new Chunk(invoice.getCustomer().getName() + "\n", BOLD_FONT));
            if (invoice.getCustomer().getAddress() != null && !invoice.getCustomer().getAddress().trim().isEmpty()) {
                customerInfo.add(new Chunk(invoice.getCustomer().getAddress() + "\n", NORMAL_FONT));
            }
            if (invoice.getCustomer().getEmail() != null) {
                customerInfo.add(new Chunk(invoice.getCustomer().getEmail() + "\n", NORMAL_FONT));
            }
            if (invoice.getCustomer().getPhone() != null && !invoice.getCustomer().getPhone().trim().isEmpty()) {
                customerInfo.add(new Chunk(invoice.getCustomer().getPhone(), NORMAL_FONT));
            }
            customerInfo.setSpacingAfter(20);
            document.add(customerInfo);

            // Line items table
            PdfPTable lineItemsTable = new PdfPTable(4);
            lineItemsTable.setWidthPercentage(100);
            lineItemsTable.setWidths(new float[]{3, 1, 1.5f, 1.5f});
            
            // Table header
            addTableHeader(lineItemsTable, "Description");
            addTableHeader(lineItemsTable, "Quantity");
            addTableHeader(lineItemsTable, "Unit Price");
            addTableHeader(lineItemsTable, "Subtotal");
            
            // Line items
            for (LineItem item : invoice.getLineItems()) {
                addTableCell(lineItemsTable, item.getDescription(), NORMAL_FONT);
                addTableCell(lineItemsTable, String.valueOf(item.getQuantity()), NORMAL_FONT, Element.ALIGN_RIGHT);
                addTableCell(lineItemsTable, CURRENCY_FORMATTER.format(item.getUnitPrice()), NORMAL_FONT, Element.ALIGN_RIGHT);
                addTableCell(lineItemsTable, CURRENCY_FORMATTER.format(item.calculateSubtotal()), NORMAL_FONT, Element.ALIGN_RIGHT);
            }
            
            document.add(lineItemsTable);

            // Totals section
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(50);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalsTable.setWidths(new float[]{1, 1});
            
            addTableCell(totalsTable, "Subtotal:", NORMAL_FONT, Element.ALIGN_RIGHT);
            addTableCell(totalsTable, CURRENCY_FORMATTER.format(invoice.getTotalAmount()), NORMAL_FONT, Element.ALIGN_RIGHT);
            
            BigDecimal paidAmount = invoice.getTotalAmount().subtract(invoice.getBalance());
            if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                addTableCell(totalsTable, "Paid:", NORMAL_FONT, Element.ALIGN_RIGHT);
                addTableCell(totalsTable, CURRENCY_FORMATTER.format(paidAmount), NORMAL_FONT, Element.ALIGN_RIGHT);
            }
            
            addTableCell(totalsTable, "Balance:", BOLD_FONT, Element.ALIGN_RIGHT);
            addTableCell(totalsTable, CURRENCY_FORMATTER.format(invoice.getBalance()), BOLD_FONT, Element.ALIGN_RIGHT);
            
            totalsTable.setSpacingBefore(10);
            document.add(totalsTable);

            // Status
            Paragraph status = new Paragraph("Status: " + invoice.getStatus().name(), NORMAL_FONT);
            status.setSpacingBefore(20);
            status.setAlignment(Element.ALIGN_CENTER);
            document.add(status);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
        
        return baos.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADING_FONT));
        cell.setBackgroundColor(new Color(0.9f, 0.9f, 0.9f)); // Light gray
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        addTableCell(table, text, font, Element.ALIGN_LEFT);
    }

    private void addTableCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM | Rectangle.LEFT | Rectangle.RIGHT);
        table.addCell(cell);
    }

    private String formatInvoiceNumber(UUID id) {
        return "INV-" + id.toString().substring(0, 8).toUpperCase();
    }
}

