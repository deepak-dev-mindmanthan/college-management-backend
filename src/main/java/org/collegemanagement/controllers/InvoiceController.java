package org.collegemanagement.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.collegemanagement.api.response.ApiResponse;
import org.collegemanagement.dto.invoice.InvoiceResponse;
import org.collegemanagement.dto.invoice.InvoiceSummaryResponse;
import org.collegemanagement.enums.InvoiceStatus;
import org.collegemanagement.services.InvoiceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/invoices")
@AllArgsConstructor
@Tag(name = "Invoice Management", description = "APIs for managing subscription invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Operation(
            summary = "Generate invoice",
            description = "Generates a new invoice for a subscription. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PostMapping("/generate/{subscriptionUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generateInvoice(
            @Parameter(description = "UUID of the subscription")
            @PathVariable String subscriptionUuid
    ) {
        InvoiceResponse invoice = invoiceService.generateInvoice(subscriptionUuid);
        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice generated successfully"));
    }

    @Operation(
            summary = "Get invoice by UUID",
            description = "Retrieves invoice information by UUID. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/{invoiceUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(
            @Parameter(description = "UUID of the invoice")
            @PathVariable String invoiceUuid
    ) {
        InvoiceResponse invoice = invoiceService.getInvoiceByUuid(invoiceUuid);
        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice retrieved successfully"));
    }

    @Operation(
            summary = "Get invoice by invoice number",
            description = "Retrieves invoice information by invoice number. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/number/{invoiceNumber}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByNumber(
            @Parameter(description = "Invoice number")
            @PathVariable String invoiceNumber
    ) {
        InvoiceResponse invoice = invoiceService.getInvoiceByInvoiceNumber(invoiceNumber);
        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice retrieved successfully"));
    }

    @Operation(
            summary = "Get all invoices",
            description = "Retrieves a paginated list of all invoices. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getAllInvoices(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<InvoiceResponse> invoices = invoiceService.getAllInvoices(pageable);
        return ResponseEntity.ok(ApiResponse.success(invoices, "Invoices retrieved successfully"));
    }

    @Operation(
            summary = "Get invoices by subscription",
            description = "Retrieves all invoices for a specific subscription. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/subscription/{subscriptionUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getInvoicesBySubscription(
            @Parameter(description = "UUID of the subscription")
            @PathVariable String subscriptionUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponse> invoices = invoiceService.getInvoicesBySubscriptionUuid(subscriptionUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(invoices, "Invoices retrieved successfully"));
    }

    @Operation(
            summary = "Get invoices by status",
            description = "Retrieves invoices filtered by status. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getInvoicesByStatus(
            @Parameter(description = "Invoice status")
            @PathVariable InvoiceStatus status,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponse> invoices = invoiceService.getInvoicesByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(invoices, "Invoices retrieved successfully"));
    }

    @Operation(
            summary = "Get overdue invoices",
            description = "Retrieves all overdue invoices. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getOverdueInvoices(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponse> invoices = invoiceService.getOverdueInvoices(pageable);
        return ResponseEntity.ok(ApiResponse.success(invoices, "Overdue invoices retrieved successfully"));
    }

    @Operation(
            summary = "Get unpaid invoices",
            description = "Retrieves all unpaid invoices. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/unpaid")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getUnpaidInvoices(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponse> invoices = invoiceService.getUnpaidInvoices(pageable);
        return ResponseEntity.ok(ApiResponse.success(invoices, "Unpaid invoices retrieved successfully"));
    }

    @Operation(
            summary = "Get invoices by due date range",
            description = "Retrieves invoices within a due date range. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/due-date-range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getInvoicesByDueDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponse> invoices = invoiceService.getInvoicesByDueDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(invoices, "Invoices retrieved successfully"));
    }

    @Operation(
            summary = "Get invoice summary",
            description = "Retrieves invoice summary statistics. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceSummaryResponse>> getInvoiceSummary() {
        InvoiceSummaryResponse summary = invoiceService.getInvoiceSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Invoice summary retrieved successfully"));
    }
}

