package org.collegemanagement.services;

import org.collegemanagement.dto.payment.CreatePaymentRequest;
import org.collegemanagement.dto.payment.PaymentResponse;
import org.collegemanagement.dto.payment.ProcessPaymentRequest;
import org.collegemanagement.enums.PaymentGateway;
import org.collegemanagement.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    /**
     * Create a new payment record
     */
    PaymentResponse createPayment(CreatePaymentRequest request);

    /**
     * Process payment (update status)
     */
    PaymentResponse processPayment(ProcessPaymentRequest request);

    /**
     * Get payment by UUID
     */
    PaymentResponse getPaymentByUuid(String paymentUuid);

    /**
     * Get payment by transaction ID
     */
    PaymentResponse getPaymentByTransactionId(String transactionId);

    /**
     * Get all payments with pagination
     */
    Page<PaymentResponse> getAllPayments(Pageable pageable);

    /**
     * Get payments by invoice UUID
     */
    Page<PaymentResponse> getPaymentsByInvoiceUuid(String invoiceUuid, Pageable pageable);

    /**
     * Get payments by status
     */
    Page<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable);

    /**
     * Get payments by gateway
     */
    Page<PaymentResponse> getPaymentsByGateway(PaymentGateway gateway, Pageable pageable);

    /**
     * Get payment summary statistics
     */
    PaymentSummary getPaymentSummary();
}

