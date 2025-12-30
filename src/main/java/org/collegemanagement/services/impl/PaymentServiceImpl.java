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
import org.collegemanagement.services.PaymentGatewayService;
import org.collegemanagement.services.PaymentService;
import org.collegemanagement.dto.PaymentSummary;
import org.collegemanagement.services.SubscriptionService;
import org.collegemanagement.services.EmailService;
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
    private final PaymentGatewayService paymentGatewayService;
    private final SubscriptionService subscriptionService;
    private final EmailService emailService;

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

        // Create payment record (status will be PENDING initially)
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
    public PaymentResponse initiatePayment(CreatePaymentRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find invoice and validate college isolation
        Invoice invoice = invoiceRepository.findByUuidAndCollegeId(request.getInvoiceUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with UUID: " + request.getInvoiceUuid()));

        // Validate transaction ID uniqueness
        if (paymentRepository.existsByTransactionId(request.getTransactionId())) {
            throw new ResourceConflictException("Payment with transaction ID already exists: " + request.getTransactionId());
        }

        // Create payment record with PENDING status
        Payment payment = Payment.builder()
                .invoice(invoice)
                .gateway(request.getGateway())
                .transactionId(request.getTransactionId())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        // Process payment through gateway
        // TODO: Integrate payment gateway
        // This will call the actual payment gateway service which currently returns SUCCESS by default
        PaymentStatus gatewayStatus = paymentGatewayService.processPayment(
                payment,
                request.getGateway(),
                request.getAmount(),
                request.getTransactionId()
        );

        // Update payment status based on gateway response
        payment.setStatus(gatewayStatus);
        payment = paymentRepository.save(payment);

        // Payment Success Block:
        // =======================
        // Handle successful payment
        if (gatewayStatus == PaymentStatus.SUCCESS) {
            updateInvoiceOnPaymentSuccess(payment, invoice, collegeId);
        }
        // Payment Failure Block:
        // ======================
        // Handle failed payment
        else if (gatewayStatus == PaymentStatus.FAILED) {
            // TODO: Integrate payment gateway
            // - Log failure reason
            // - Send notification to user
            // - Update any retry logic if applicable
            log.warn("Payment failed for transaction: {}", request.getTransactionId());
        }
        // Payment Pending Block:
        // ======================
        // Handle pending payment (e.g., 3D Secure authentication required)
        else if (gatewayStatus == PaymentStatus.PENDING) {
            // TODO: Integrate payment gateway
            // - Store gateway response for later verification
            // - Set up webhook listener for status updates
            // - Return appropriate response to client for further action
            log.info("Payment pending for transaction: {}", request.getTransactionId());
        }

        return mapToResponse(payment);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Payment payment = paymentRepository.findByUuidAndCollegeId(request.getPaymentUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with UUID: " + request.getPaymentUuid()));

        // If status is provided in request, use it (manual update)
        // Otherwise, verify with gateway
        PaymentStatus newStatus = request.getStatus();
        
        if (newStatus == null) {
            // TODO: Integrate payment gateway
            // Verify payment status with gateway
            newStatus = paymentGatewayService.verifyPaymentStatus(
                    payment,
                    payment.getGateway(),
                    payment.getTransactionId()
            );
        }

        // Update payment status
        payment.setStatus(newStatus);

        // Payment Success Block:
        // ======================
        // Handle successful payment
        if (newStatus == PaymentStatus.SUCCESS) {
            Invoice invoice = payment.getInvoice();
            updateInvoiceOnPaymentSuccess(payment, invoice, collegeId);
        }
        // Payment Failure Block:
        // ======================
        // Handle failed payment
        else if (newStatus == PaymentStatus.FAILED) {
            // TODO: Integrate payment gateway
            // - Log failure reason from request if provided
            // - Send notification to user
            // - Update any retry logic if applicable
            if (request.getFailureReason() != null) {
                log.warn("Payment failed for transaction: {}. Reason: {}", 
                        payment.getTransactionId(), request.getFailureReason());
            }
            
            // Send payment failure email
            try {
                Invoice invoice = payment.getInvoice();
                emailService.sendPaymentFailureEmail(
                        invoice.getCollege().getEmail(),
                        invoice.getCollege().getName(),
                        invoice.getInvoiceNumber(),
                        request.getFailureReason() != null ? request.getFailureReason() : "Payment processing failed"
                );
            } catch (Exception e) {
                log.warn("Failed to send payment failure email: {}", e.getMessage());
            }
        }
        // Payment Pending Block:
        // ======================
        // Handle pending payment
        else if (newStatus == PaymentStatus.PENDING) {
            // TODO: Integrate payment gateway
            // - Payment is still pending (e.g., waiting for 3D Secure)
            // - Set up webhook listener for status updates
            log.info("Payment still pending for transaction: {}", payment.getTransactionId());
        }

        payment = paymentRepository.save(payment);

        return mapToResponse(payment);
    }

    /**
     * Helper method to update invoice status when payment is successful.
     * 
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
                        payment.getTransactionId()
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

