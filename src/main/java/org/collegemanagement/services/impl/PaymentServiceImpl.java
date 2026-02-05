package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.PaymentSummary;
import org.collegemanagement.dto.payment.ConfirmPaymentRequest;
import org.collegemanagement.dto.payment.InitiatePaymentRequest;
import org.collegemanagement.dto.payment.PaymentResponse;
import org.collegemanagement.entity.finance.Invoice;
import org.collegemanagement.entity.finance.Payment;
import org.collegemanagement.entity.subscription.Subscription;
import org.collegemanagement.enums.*;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.repositories.InvoiceRepository;
import org.collegemanagement.repositories.PaymentRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.EmailService;
import org.collegemanagement.services.PaymentService;
import org.collegemanagement.services.SubscriptionService;
import org.collegemanagement.services.gateway.PaymentGatewayClient;
import org.collegemanagement.services.gateway.PaymentGatewayFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final PaymentGatewayFactory paymentGatewayFactory;
    private final SubscriptionService subscriptionService;
    private final EmailService emailService;

    /* =========================================================
       INITIATE PAYMENT (ORDER CREATION)
       ========================================================= */
    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public PaymentResponse initiatePayment(InitiatePaymentRequest request) {

        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Invoice invoice = fetchInvoice(request.getInvoiceUuid(), collegeId);

        Payment payment = createPendingPayment(invoice, request.getGateway());

        PaymentGatewayClient gatewayClient =
                paymentGatewayFactory.getClient(request.getGateway());

        String gatewayOrderId =
                gatewayClient.createOrder(invoice.getInvoiceNumber(), payment.getAmount());

        payment.setGatewayOrderId(gatewayOrderId);
        payment.setGatewayTransactionId("transaction_gw_txn_id_from_payload");

        paymentRepository.save(payment);

        return mapToResponse(payment);
    }

    /* =========================================================
       CONFIRM PAYMENT (WEBHOOK / CALLBACK)
       ========================================================= */
    @Override
    public void confirmPayment(ConfirmPaymentRequest request) {

        Payment payment = fetchPaymentByGatewayOrderId(request.getGatewayOrderId());

        // Idempotent webhook handling
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("Payment already processed successfully. Skipping.");
            return;
        }

        updatePaymentFromWebhook(payment, request);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            processSuccessfulPayment(payment);
        } else if (payment.getStatus() == PaymentStatus.FAILED) {
            processFailedPayment(payment, request.getFailureReason());
        }
    }

    /* =========================================================
       SUCCESS PAYMENT FLOW
       ========================================================= */
    private void processSuccessfulPayment(Payment payment) {

        Invoice invoice = payment.getInvoice();
        Long collegeId = invoice.getCollege().getId();

        markInvoicePaidIfFullySettled(invoice, collegeId);

        activateSubscriptionIfNeeded(invoice);

        sendPaymentSuccessEmail(invoice, payment);
    }

    /* =========================================================
       FAILED PAYMENT FLOW
       ========================================================= */
    private void processFailedPayment(Payment payment, String reason) {

        payment.setFailureReason(reason != null ? reason : "Payment failed");
        paymentRepository.save(payment);

        sendPaymentFailureEmail(payment, reason);

        log.warn("Payment FAILED | OrderId={} | Reason={}",
                payment.getGatewayOrderId(), reason);
    }

    /* =========================================================
       HELPERS (SINGLE RESPONSIBILITY)
       ========================================================= */

    private Invoice fetchInvoice(String uuid, Long collegeId) {
        return invoiceRepository.findByUuidAndCollegeId(uuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }

    private Payment fetchPaymentByGatewayOrderId(String gatewayOrderId) {
        return paymentRepository.findByGatewayOrderId(gatewayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    private Payment createPendingPayment(Invoice invoice, PaymentGateway gateway) {

        Payment payment = Payment.builder()
                .invoice(invoice)
                .gateway(gateway)
                .amount(invoice.getOutStandingAmount())
                .status(PaymentStatus.PENDING)
                .build();

        return paymentRepository.save(payment);
    }

    private void updatePaymentFromWebhook(Payment payment, ConfirmPaymentRequest request) {

        payment.setGatewayTransactionId(request.getGatewayTransactionId());
        payment.setStatus(request.getStatus());
        payment.setPaymentDate(Instant.now());

        paymentRepository.save(payment);
    }

    private void markInvoicePaidIfFullySettled(Invoice invoice, Long collegeId) {

        BigDecimal totalPaid = paymentRepository
                .findByInvoiceIdAndCollegeId(invoice.getId(), collegeId)
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(invoice.getAmount()) >= 0) {

            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(Instant.now());

            invoiceRepository.save(invoice);

            log.info("Invoice {} marked as PAID", invoice.getInvoiceNumber());
        }
    }

    private void activateSubscriptionIfNeeded(Invoice invoice) {

        Subscription subscription = invoice.getSubscription();

        if (subscription != null &&
                subscription.getStatus() == SubscriptionStatus.PENDING) {

            log.info("Activating subscription {} after successful payment", subscription.getUuid());

            // Webhook-safe internal method
            subscriptionService.activateSubscriptionFromSystem(subscription.getUuid(), invoice.getCollege().getId());
        }
    }

    private void sendPaymentSuccessEmail(Invoice invoice, Payment payment) {

        emailService.sendPaymentConfirmationEmail(
                invoice.getCollege().getEmail(),
                invoice.getCollege().getName(),
                invoice.getInvoiceNumber(),
                payment.getAmount(),
                payment.getGatewayTransactionId()
        );
    }

    private void sendPaymentFailureEmail(Payment payment, String reason) {

        Invoice invoice = payment.getInvoice();

        emailService.sendPaymentFailureEmail(
                invoice.getCollege().getEmail(),
                invoice.getCollege().getName(),
                invoice.getInvoiceNumber(),
                reason != null ? reason : "Payment failed"
        );
    }

    /* =========================================================
       READ APIs (Unchanged, Clean)
       ========================================================= */

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public PaymentResponse getPaymentByUuid(String uuid) {

        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Payment payment = paymentRepository.findByUuidAndCollegeId(uuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        return mapToResponse(payment);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<PaymentResponse> getPaymentsByInvoiceUuid(String invoiceUuid, Pageable pageable) {

        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Invoice invoice = fetchInvoice(invoiceUuid, collegeId);

        return paymentRepository.findByInvoiceIdAndCollegeId(invoice.getId(), collegeId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        return paymentRepository
                .findByStatusAndCollegeId(status, tenantAccessGuard.getCurrentTenantId(), pageable)
                .map(this::mapToResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<PaymentResponse> getPaymentsByGateway(PaymentGateway gateway, Pageable pageable) {
        return paymentRepository
                .findByGatewayAndCollegeId(gateway, tenantAccessGuard.getCurrentTenantId(), pageable)
                .map(this::mapToResponse);
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
    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Payment payment = paymentRepository.findByTransactionIdAndCollegeId(transactionId, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction ID: " + transactionId));

        return mapToResponse(payment);
    }

    /* =========================================================
       PAYMENT SUMMARY
       ========================================================= */
    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public PaymentSummary getPaymentSummary() {

        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        long successfulPayments =
                paymentRepository.countByStatusAndCollegeId(PaymentStatus.SUCCESS, collegeId);

        long pendingPayments =
                paymentRepository.countByStatusAndCollegeId(PaymentStatus.PENDING, collegeId);

        long failedPayments =
                paymentRepository.countByStatusAndCollegeId(PaymentStatus.FAILED, collegeId);

        return PaymentSummary.builder()
                .successfulPayments(successfulPayments)
                .pendingPayments(pendingPayments)
                .failedPayments(failedPayments)
                .build();
    }

    /* =========================================================
       RESPONSE MAPPER
       ========================================================= */
    private PaymentResponse mapToResponse(Payment payment) {

        Invoice invoice = payment.getInvoice();

        return PaymentResponse.builder()
                .uuid(payment.getUuid())
                .invoiceUuid(invoice.getUuid())
                .invoiceNumber(invoice.getInvoiceNumber())
                .gateway(payment.getGateway())
                .gatewayOrderId(payment.getGatewayOrderId())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt().toInstant(ZoneOffset.UTC))
                .updatedAt(payment.getUpdatedAt().toInstant(ZoneOffset.UTC))
                .build();
    }
}
