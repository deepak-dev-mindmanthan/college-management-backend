package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.PaymentSummary;
import org.collegemanagement.dto.payment.ConfirmPaymentRequest;
import org.collegemanagement.dto.payment.InitiatePaymentRequest;
import org.collegemanagement.dto.payment.PaymentResponse;
import org.collegemanagement.entity.finance.Invoice;
import org.collegemanagement.entity.finance.Payment;
import org.collegemanagement.enums.PaymentGateway;
import org.collegemanagement.enums.PaymentStatus;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.repositories.InvoiceRepository;
import org.collegemanagement.repositories.PaymentRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.EmailService;
import org.collegemanagement.services.PaymentGatewayService;
import org.collegemanagement.services.PaymentService;
import org.collegemanagement.services.SubscriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final PaymentGatewayService paymentGatewayService;
    private final SubscriptionService subscriptionService;
    private final EmailService emailService;

    /* =========================================================
         INITIATE PAYMENT (ORDER CREATION)
         ========================================================= */
    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public PaymentResponse initiatePayment(InitiatePaymentRequest request) {

        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Invoice invoice = invoiceRepository
                .findByUuidAndCollegeId(request.getInvoiceUuid(), collegeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invoice not found")
                );

        Payment payment = Payment.builder()
                .invoice(invoice)
                .gateway(request.getGateway())
                .amount(invoice.getOutStandingAmount())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        String gatewayOrderId =
                paymentGatewayService.createOrder(payment, payment.getAmount());

        payment.setGatewayOrderId(gatewayOrderId);
        paymentRepository.save(payment);

        return mapToResponse(payment);
    }


    /* =========================================================
       CONFIRM PAYMENT (WEBHOOK / CALLBACK)
       ========================================================= */
    @Override
    public void confirmPayment(ConfirmPaymentRequest request) {

        Payment payment = paymentRepository
                .findByGatewayOrderId(request.getGatewayOrderId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found")
                );

        // Idempotency
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        payment.setGatewayTransactionId(request.getGatewayTransactionId());
        payment.setStatus(request.getStatus());
        payment.setPaymentDate(Instant.now());

        paymentRepository.save(payment);

        if (request.getStatus() == PaymentStatus.SUCCESS) {
            handleSuccess(payment);
        } else if (request.getStatus() == PaymentStatus.FAILED) {
            handleFailure(payment, request.getFailureReason());
        }
    }

    /* =========================================================
       SUCCESS HANDLER
       ========================================================= */
    private void handleSuccess(Payment payment) {

        Invoice invoice = payment.getInvoice();
        Long collegeId = invoice.getCollege().getId();

        BigDecimal totalPaid =
                paymentRepository.findByInvoiceIdAndCollegeId(invoice.getId(), collegeId)
                        .stream()
                        .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(invoice.getAmount()) >= 0) {
            invoice.setStatus(org.collegemanagement.enums.InvoiceStatus.PAID);
            invoice.setPaidAt(Instant.now());
            invoiceRepository.save(invoice);
        }

        if (invoice.getSubscription() != null) {
            subscriptionService.activateSubscription(
                    invoice.getSubscription().getUuid()
            );
        }

        emailService.sendPaymentConfirmationEmail(
                invoice.getCollege().getEmail(),
                invoice.getCollege().getName(),
                invoice.getInvoiceNumber(),
                payment.getAmount(),
                payment.getGatewayTransactionId()
        );
    }


    /* =========================================================
      FAILURE HANDLER
      ========================================================= */
    private void handleFailure(Payment payment, String reason) {
        Invoice invoice = payment.getInvoice();
        emailService.sendPaymentFailureEmail(
                invoice.getCollege().getEmail(),
                invoice.getCollege().getName(),
                invoice.getInvoiceNumber(),
                reason != null ? reason : "Payment failed"
        );
    }


    /**
     * Helper method to update invoice status when payment is successful.
     * <p>
     * Payment Success Block:
     * ======================
     * This method handles the business logic when a payment succeeds:
     * - Calculates total paid amount
     * - Marks invoice as PAID if fully paid
     * - Automatically activates subscription if payment is for subscription invoice
     * - Sends confirmation email
     */
    private void updateInvoiceOnPaymentSuccess(Payment payment, Invoice invoice, Long collegeId) {
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
            invoiceRepository.save(invoice);

            // Payment Success Block - Automatic Subscription Activation:
            // ===========================================================
            // Automatically activate subscription if payment is for subscription invoice
            try {
                org.collegemanagement.entity.subscription.Subscription subscription = invoice.getSubscription();
                if (subscription != null && subscription.getStatus() == org.collegemanagement.enums.SubscriptionStatus.PENDING) {
                    log.info("Auto-activating subscription {} after successful payment", subscription.getUuid());
                    subscriptionService.activateSubscription(subscription.getUuid());

                    // Send activation email notification
                    try {
                        emailService.sendSubscriptionActivatedEmail(
                                invoice.getCollege().getEmail(),
                                invoice.getCollege().getName(),
                                subscription.getPlan().getCode().name(),
                                subscription.getExpiresAt()
                        );
                    } catch (Exception e) {
                        log.warn("Failed to send subscription activation email: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to auto-activate subscription after payment: {}", e.getMessage());
                // Don't fail the payment process if activation fails
            }

            // Send payment confirmation email
            try {
                emailService.sendPaymentConfirmationEmail(
                        invoice.getCollege().getEmail(),
                        invoice.getCollege().getName(),
                        invoice.getInvoiceNumber(),
                        payment.getAmount(),
                        payment.getGatewayTransactionId()
                );
            } catch (Exception e) {
                log.warn("Failed to send payment confirmation email: {}", e.getMessage());
            }

            // TODO: Integrate payment gateway
            // - Additional gateway-specific post-payment workflows can be added here
            // - Gateway webhook processing
            // - Third-party integrations
        }
    }

    /* =========================================================
        READ APIS
        ========================================================= */
    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public PaymentResponse getPaymentByUuid(String uuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Payment payment = paymentRepository
                .findByUuidAndCollegeId(uuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return mapToResponse(payment);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<PaymentResponse> getPaymentsByInvoiceUuid(String invoiceUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Invoice invoice = invoiceRepository
                .findByUuidAndCollegeId(invoiceUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return paymentRepository
                .findByInvoiceIdAndCollegeId(invoice.getId(), collegeId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        return paymentRepository.findByStatusAndCollegeId(status,tenantAccessGuard.getCurrentTenantId(), pageable).map(this::mapToResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<PaymentResponse> getPaymentsByGateway(PaymentGateway gateway, Pageable pageable) {
        return paymentRepository.findByGatewayAndCollegeId(gateway,tenantAccessGuard.getCurrentTenantId(), pageable).map(this::mapToResponse);
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
                .gatewayOrderId(payment.getGatewayOrderId())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt() != null ? payment.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null)
                .updatedAt(payment.getUpdatedAt() != null ? payment.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null)

                .build();
    }
}

