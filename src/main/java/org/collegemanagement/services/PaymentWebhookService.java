package org.collegemanagement.services;

import org.collegemanagement.enums.PaymentGateway;

/**
 * Service interface for handling payment gateway webhooks.
 * 
 * TODO: Integrate payment gateway webhooks
 * - Implement webhook endpoint handlers for each gateway
 * - Verify webhook signatures for security
 * - Handle payment status updates from gateway
 * - Process refund webhooks
 * - Handle dispute/chargeback webhooks
 */
public interface PaymentWebhookService {

    /**
     * Handle webhook event from payment gateway.
     * 
     * @param gateway The payment gateway that sent the webhook
     * @param payload The webhook payload (JSON string or object)
     * @param signature The webhook signature for verification
     * @return true if webhook was processed successfully, false otherwise
     * 
     * TODO: Integrate payment gateway webhooks
     * 
     * Payment Success Webhook Block:
     * ===============================
     * 1. Verify webhook signature
     * 2. Parse webhook payload
     * 3. Extract transaction ID and payment status
     * 4. Find payment by transaction ID
     * 5. Update payment status to SUCCESS
     * 6. Update invoice status if fully paid
     * 7. Activate subscription if applicable
     * 8. Send confirmation notification
     * 
     * Payment Failure Webhook Block:
     * ==============================
     * 1. Verify webhook signature
     * 2. Parse webhook payload
     * 3. Extract transaction ID and failure reason
     * 4. Find payment by transaction ID
     * 5. Update payment status to FAILED
     * 6. Log failure reason
     * 7. Send failure notification to user
     * 
     * Refund Webhook Block:
     * =====================
     * 1. Verify webhook signature
     * 2. Parse refund details
     * 3. Create refund record
     * 4. Update payment/invoice status
     * 5. Handle partial vs full refunds
     */
    boolean handleWebhook(PaymentGateway gateway, String payload, String signature);

    /**
     * Verify webhook signature for security.
     * 
     * @param gateway The payment gateway
     * @param payload The webhook payload
     * @param signature The webhook signature
     * @return true if signature is valid, false otherwise
     * 
     * TODO: Integrate payment gateway webhook verification
     * - Implement Razorpay signature verification
     * - Implement Stripe signature verification
     * - Use gateway-specific secret keys from configuration
     */
    boolean verifyWebhookSignature(PaymentGateway gateway, String payload, String signature);
}

