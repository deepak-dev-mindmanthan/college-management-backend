package org.collegemanagement.services;

import org.collegemanagement.dto.invoice.InvoiceResponse;
import org.collegemanagement.dto.invoice.InvoiceSummaryResponse;
import org.collegemanagement.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface InvoiceService {

    /**
     * Generate invoice for a subscription
     */
    InvoiceResponse generateInvoice(String subscriptionUuid);

    /**
     * Get invoice by UUID
     */
    InvoiceResponse getInvoiceByUuid(String invoiceUuid);

    /**
     * Get invoice by invoice number
     */
    InvoiceResponse getInvoiceByInvoiceNumber(String invoiceNumber);

    /**
     * Get all invoices with pagination
     */
    Page<InvoiceResponse> getAllInvoices(Pageable pageable);

    /**
     * Get invoices by subscription UUID
     */
    Page<InvoiceResponse> getInvoicesBySubscriptionUuid(String subscriptionUuid, Pageable pageable);

    /**
     * Get invoices by status
     */
    Page<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status, Pageable pageable);

    /**
     * Get overdue invoices
     */
    Page<InvoiceResponse> getOverdueInvoices(Pageable pageable);

    /**
     * Get unpaid invoices
     */
    Page<InvoiceResponse> getUnpaidInvoices(Pageable pageable);

    /**
     * Get invoices by due date range
     */
    Page<InvoiceResponse> getInvoicesByDueDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Get invoice summary statistics
     */
    InvoiceSummaryResponse getInvoiceSummary();
}

