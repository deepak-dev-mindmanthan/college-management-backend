package org.collegemanagement.services;

/**
 * Service for handling subscription renewal automation.
 * 
 * TODO: Integrate scheduled job for automatic renewal
 * - Check subscriptions expiring soon
 * - Generate renewal invoices automatically
 * - Send renewal reminders
 */
public interface SubscriptionRenewalService {

    /**
     * Check and process subscriptions expiring soon
     * Should be called by scheduled job
     */
    void processExpiringSubscriptions();

    /**
     * Generate renewal invoice for a subscription
     */
    void generateRenewalInvoice(String subscriptionUuid);

    /**
     * Send renewal reminder emails
     */
    void sendRenewalReminders(int daysBeforeExpiry);
}

