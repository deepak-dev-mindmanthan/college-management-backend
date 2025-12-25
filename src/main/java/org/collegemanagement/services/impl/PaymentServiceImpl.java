package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.payment.CreatePaymentRequest;
import org.collegemanagement.dto.payment.PaymentResponse;
import org.collegemanagement.dto.payment.ProcessPaymentRequest;
import org.collegemanagement.entity.finance.Invoice;
import org.collegemanagement.entity.finance.Payment;
import org.collegemanagement.enums.PaymentGateway;
import org.collegemanagement.enums.PaymentStatus;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.repositories.InvoiceRepository;
import org.collegemanagement.repositories.PaymentRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.PaymentService;
import org.collegemanagement.services.PaymentSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final TenantAccessGuard tenantAccessGuard;

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find invoice and validate college isolation
        Invoice invoice = invoiceRepository.findByUuidAndCollegeId(request.getInvoiceUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with UUID: " + request.getInvoiceUuid()));

        // Validate transaction ID uniqueness
        if (paymentRepository.existsByTransactionId(request.getTransactionId())) {
            throw new ResourceConflictException("Payment with transaction ID already exists: " + request.getTransactionId());
        }

        // Create payment
        Payment payment = Payment.builder()
                .invoice(invoice)
                .gateway(request.getGateway())
                .transactionId(request.getTransactionId())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        return mapToResponse(payment);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Payment payment = paymentRepository.findByUuidAndCollegeId(request.getPaymentUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with UUID: " + request.getPaymentUuid()));

        // Update payment status
        payment.setStatus(request.getStatus());

        // If payment is successful, update invoice status
        if (request.getStatus() == PaymentStatus.SUCCESS) {
            Invoice invoice = payment.getInvoice();
            // Check if invoice is fully paid
            BigDecimal totalPaid = paymentRepository.findByInvoiceIdAndCollegeId(invoice.getId(), collegeId)
                    .stream()
                    .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalPaid = totalPaid.add(payment.getAmount());

            if (totalPaid.compareTo(invoice.getAmount()) >= 0) {
                invoice.setStatus(org.collegemanagement.enums.InvoiceStatus.PAID);
                invoice.setPaidAt(java.time.Instant.now());
            }
            invoiceRepository.save(invoice);
        }

        payment = paymentRepository.save(payment);

        return mapToResponse(payment);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public PaymentResponse getPaymentByUuid(String paymentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Payment payment = paymentRepository.findByUuidAndCollegeId(paymentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with UUID: " + paymentUuid));

        return mapToResponse(payment);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Payment payment = paymentRepository.findByTransactionIdAndCollegeId(transactionId, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction ID: " + transactionId));

        return mapToResponse(payment);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        return paymentRepository.findAllByCollegeId(collegeId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<PaymentResponse> getPaymentsByInvoiceUuid(String invoiceUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Invoice invoice = invoiceRepository.findByUuidAndCollegeId(invoiceUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with UUID: " + invoiceUuid));

        List<Payment> payments = paymentRepository.findByInvoiceIdAndCollegeId(invoice.getId(), collegeId);
        // Convert to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), payments.size());
        List<Payment> pagedPayments = payments.subList(start, end);

        List<PaymentResponse> responses = pagedPayments.stream()
                .map(this::mapToResponse)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                responses,
                pageable,
                payments.size()
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        return paymentRepository.findByStatusAndCollegeId(status, collegeId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<PaymentResponse> getPaymentsByGateway(PaymentGateway gateway, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        return paymentRepository.findByGatewayAndCollegeId(gateway, collegeId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public PaymentSummary getPaymentSummary() {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        long totalPayments = paymentRepository.count();
        long successfulPayments = paymentRepository.countByStatusAndCollegeId(PaymentStatus.SUCCESS, collegeId);
        long pendingPayments = paymentRepository.countByStatusAndCollegeId(PaymentStatus.PENDING, collegeId);
        long failedPayments = paymentRepository.countByStatusAndCollegeId(PaymentStatus.FAILED, collegeId);

        // Calculate amounts (simplified - in production, use aggregation queries)
        Page<Payment> allPayments = paymentRepository.findAllByCollegeId(collegeId, Pageable.unpaged());
        BigDecimal totalAmount = allPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal successfulAmount = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingAmount = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal failedAmount = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.FAILED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PaymentSummary.builder()
                .totalPayments(totalPayments)
                .successfulPayments(successfulPayments)
                .pendingPayments(pendingPayments)
                .failedPayments(failedPayments)
                .totalAmount(totalAmount)
                .successfulAmount(successfulAmount)
                .pendingAmount(pendingAmount)
                .failedAmount(failedAmount)
                .build();
    }

    private PaymentResponse mapToResponse(Payment payment) {
        Invoice invoice = payment.getInvoice();
        return PaymentResponse.builder()
                .uuid(payment.getUuid())
                .invoiceUuid(invoice.getUuid())
                .invoiceNumber(invoice.getInvoiceNumber())
                .gateway(payment.getGateway())
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt() != null ? payment.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null)
                .updatedAt(payment.getUpdatedAt() != null ? payment.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null)
                .build();
    }
}

