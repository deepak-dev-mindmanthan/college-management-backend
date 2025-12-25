package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.invoice.InvoiceResponse;
import org.collegemanagement.dto.invoice.InvoiceSummaryResponse;
import org.collegemanagement.entity.finance.Invoice;
import org.collegemanagement.entity.finance.Payment;
import org.collegemanagement.entity.subscription.Subscription;
import org.collegemanagement.enums.InvoiceStatus;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.repositories.InvoiceRepository;
import org.collegemanagement.repositories.PaymentRepository;
import org.collegemanagement.repositories.SubscriptionRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.InvoiceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final TenantAccessGuard tenantAccessGuard;

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public InvoiceResponse generateInvoice(String subscriptionUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Subscription subscription = subscriptionRepository.findByUuidAndCollegeId(subscriptionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with UUID: " + subscriptionUuid));

        // Check if there's already an unpaid invoice for this subscription
        List<Invoice> unpaidInvoices = invoiceRepository.findUnpaidInvoicesByCollegeId(collegeId);
        if (unpaidInvoices.stream().anyMatch(inv -> inv.getSubscription().getId().equals(subscription.getId()))) {
            throw new org.collegemanagement.exception.ResourceConflictException(
                    "An unpaid invoice already exists for this subscription");
        }

        // Generate invoice number
        String invoiceNumber = generateInvoiceNumber();

        // Calculate period dates
        LocalDate periodStart = subscription.getStartsAt();
        LocalDate periodEnd = subscription.getExpiresAt();
        LocalDate dueDate = periodStart.plusDays(7); // Due date is 7 days from start

        // Create invoice
        Invoice invoice = Invoice.builder()
                .college(subscription.getCollege())
                .subscription(subscription)
                .invoiceNumber(invoiceNumber)
                .amount(subscription.getPlan().getPrice())
                .currency(subscription.getPlan().getCurrency().name())
                .status(InvoiceStatus.UNPAID)
                .dueDate(dueDate)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .build();

        invoice = invoiceRepository.save(invoice);

        return mapToResponse(invoice, collegeId);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public InvoiceResponse getInvoiceByUuid(String invoiceUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Invoice invoice = invoiceRepository.findByUuidAndCollegeId(invoiceUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with UUID: " + invoiceUuid));

        return mapToResponse(invoice, collegeId);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public InvoiceResponse getInvoiceByInvoiceNumber(String invoiceNumber) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Invoice invoice = invoiceRepository.findByInvoiceNumberAndCollegeId(invoiceNumber, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with invoice number: " + invoiceNumber));

        return mapToResponse(invoice, collegeId);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<InvoiceResponse> getAllInvoices(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        return invoiceRepository.findAllByCollegeId(collegeId, pageable)
                .map(invoice -> mapToResponse(invoice, collegeId));
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<InvoiceResponse> getInvoicesBySubscriptionUuid(String subscriptionUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Subscription subscription = subscriptionRepository.findByUuidAndCollegeId(subscriptionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with UUID: " + subscriptionUuid));

        List<Invoice> invoices = invoiceRepository.findBySubscriptionIdAndCollegeId(subscription.getId(), collegeId);
        // Convert to page (simplified)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), invoices.size());
        List<Invoice> pagedInvoices = invoices.subList(start, end);

        List<InvoiceResponse> responses = pagedInvoices.stream()
                .map(invoice -> mapToResponse(invoice, collegeId))
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                responses,
                pageable,
                invoices.size()
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        return invoiceRepository.findByStatusAndCollegeId(status, collegeId, pageable)
                .map(invoice -> mapToResponse(invoice, collegeId));
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<InvoiceResponse> getOverdueInvoices(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoicesByCollegeId(collegeId, LocalDate.now());
        // Convert to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), overdueInvoices.size());
        List<Invoice> pagedInvoices = overdueInvoices.subList(start, end);

        List<InvoiceResponse> responses = pagedInvoices.stream()
                .map(invoice -> mapToResponse(invoice, collegeId))
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                responses,
                pageable,
                overdueInvoices.size()
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<InvoiceResponse> getUnpaidInvoices(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        List<Invoice> unpaidInvoices = invoiceRepository.findUnpaidInvoicesByCollegeId(collegeId);
        // Convert to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), unpaidInvoices.size());
        List<Invoice> pagedInvoices = unpaidInvoices.subList(start, end);

        List<InvoiceResponse> responses = pagedInvoices.stream()
                .map(invoice -> mapToResponse(invoice, collegeId))
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                responses,
                pageable,
                unpaidInvoices.size()
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<InvoiceResponse> getInvoicesByDueDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        return invoiceRepository.findByDueDateRangeAndCollegeId(collegeId, startDate, endDate, pageable)
                .map(invoice -> mapToResponse(invoice, collegeId));
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public InvoiceSummaryResponse getInvoiceSummary() {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        long totalInvoices = invoiceRepository.count();
        long paidInvoices = invoiceRepository.countByStatusAndCollegeId(InvoiceStatus.PAID, collegeId);
        long unpaidInvoices = invoiceRepository.countByStatusAndCollegeId(InvoiceStatus.UNPAID, collegeId);
        long failedInvoices = invoiceRepository.countByStatusAndCollegeId(InvoiceStatus.FAILED, collegeId);

        // Calculate amounts (simplified - in production, use aggregation queries)
        Page<Invoice> allInvoices = invoiceRepository.findAllByCollegeId(collegeId, Pageable.unpaged());
        BigDecimal totalAmount = allInvoices.stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidAmount = allInvoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.PAID)
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unpaidAmount = allInvoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.UNPAID)
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal failedAmount = allInvoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.FAILED)
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return InvoiceSummaryResponse.builder()
                .totalInvoices(totalInvoices)
                .paidInvoices(paidInvoices)
                .unpaidInvoices(unpaidInvoices)
                .failedInvoices(failedInvoices)
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .unpaidAmount(unpaidAmount)
                .failedAmount(failedAmount)
                .build();
    }

    private String generateInvoiceNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String invoiceNumber = "INV-" + datePrefix + "-" + uniqueSuffix;

        // Ensure uniqueness
        int attempts = 0;
        while (invoiceRepository.existsByInvoiceNumber(invoiceNumber) && attempts < 10) {
            uniqueSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            invoiceNumber = "INV-" + datePrefix + "-" + uniqueSuffix;
            attempts++;
        }

        if (attempts >= 10) {
            throw new IllegalStateException("Failed to generate unique invoice number");
        }

        return invoiceNumber;
    }

    private InvoiceResponse mapToResponse(Invoice invoice, Long collegeId) {
        List<Payment> payments = paymentRepository.findByInvoiceIdAndCollegeId(invoice.getId(), collegeId);
        long paymentCount = payments.size();
        BigDecimal totalPaidAmount = payments.stream()
                .filter(p -> p.getStatus() == org.collegemanagement.enums.PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return InvoiceResponse.builder()
                .uuid(invoice.getUuid())
                .invoiceNumber(invoice.getInvoiceNumber())
                .subscriptionUuid(invoice.getSubscription().getUuid())
                .planName(invoice.getSubscription().getPlan().getCode().name())
                .billingCycle(InvoiceResponse.BillingCycle.valueOf(invoice.getSubscription().getPlan().getBillingCycle().name()))
                .amount(invoice.getAmount())
                .currency(invoice.getCurrency())
                .status(invoice.getStatus())
                .dueDate(invoice.getDueDate())
                .periodStart(invoice.getPeriodStart())
                .periodEnd(invoice.getPeriodEnd())
                .paidAt(invoice.getPaidAt())
                .createdAt(invoice.getCreatedAt() != null ? invoice.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null)
                .updatedAt(invoice.getUpdatedAt() != null ? invoice.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null)
                .paymentCount(paymentCount)
                .totalPaidAmount(totalPaidAmount)
                .build();
    }
}

