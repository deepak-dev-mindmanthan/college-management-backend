package org.collegemanagement.services;

import org.collegemanagement.dto.PaymentSummary;
import org.collegemanagement.dto.payment.ConfirmPaymentRequest;
import org.collegemanagement.dto.payment.InitiatePaymentRequest;
import org.collegemanagement.dto.payment.PaymentResponse;
import org.collegemanagement.enums.PaymentGateway;
import org.collegemanagement.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {


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

    /**
     * Initiate and process payment through gateway.
     * Creates payment record and automatically processes it through the payment gateway.
     *
     * @param request Payment creation request
     * @return PaymentResponse with processed status
     */
    PaymentResponse initiatePayment(InitiatePaymentRequest request);



    /**
     * Confirm payments
     */
    void confirmPayment(ConfirmPaymentRequest request);


}

