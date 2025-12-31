package org.collegemanagement.services;

/**
 * Service interface for handling payment gateway webhooks.
 * TODO: Integrate payment gateway webhooks
 * - Implement webhook endpoint handlers for each gateway
 * - Verify webhook signatures for security
 * - Handle payment status updates from gateway
 * - Process refund webhooks
 * - Handle dispute/chargeback webhooks
 */



public interface PaymentWebhookService {

    void handleRazorpay(String payload, String signature);

    void handleStripe(String payload, String signature);
}
