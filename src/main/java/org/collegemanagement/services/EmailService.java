package org.collegemanagement.services;

import java.time.LocalDate;

/**
 * Service interface for sending emails.
 * 
 * TODO: Integrate actual email service (e.g., SendGrid, AWS SES, SMTP)
 * For now, uses Spring Mail with dummy SMTP credentials from application.yml
 */
public interface EmailService {

    /**
     * Send payment confirmation email
     */
    void sendPaymentConfirmationEmail(String toEmail, String collegeName, String invoiceNumber, 
                                     java.math.BigDecimal amount, String transactionId);

    /**
     * Send invoice generated email
     */
    void sendInvoiceGeneratedEmail(String toEmail, String collegeName, String invoiceNumber, 
                                  java.math.BigDecimal amount, LocalDate dueDate);

    /**
     * Send subscription activated email
     */
    void sendSubscriptionActivatedEmail(String toEmail, String collegeName, String planType, LocalDate expiresAt);

    /**
     * Send subscription expiring soon email
     */
    void sendSubscriptionExpiringEmail(String toEmail, String collegeName, String planType, LocalDate expiresAt, int daysRemaining);

    /**
     * Send subscription expired email
     */
    void sendSubscriptionExpiredEmail(String toEmail, String collegeName, String planType, LocalDate expiredAt);

    /**
     * Send payment failure email
     */
    void sendPaymentFailureEmail(String toEmail, String collegeName, String invoiceNumber, String failureReason);
}

