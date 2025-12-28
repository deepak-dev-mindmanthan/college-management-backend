package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.entity.subscription.Subscription;
import org.collegemanagement.enums.SubscriptionStatus;
import org.collegemanagement.repositories.SubscriptionRepository;
import org.collegemanagement.services.EmailService;
import org.collegemanagement.services.InvoiceService;
import org.collegemanagement.services.SubscriptionRenewalService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation for subscription renewal automation.
 * 
 * TODO: Integrate scheduled job
 * - Configure @EnableScheduling in main application class
 * - Add cron expression configuration
 * - Add retry logic for failed renewals
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionRenewalServiceImpl implements SubscriptionRenewalService {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceService invoiceService;
    private final EmailService emailService;

    /**
     * Scheduled job to check expiring subscriptions daily at 2 AM
     * 
     * TODO: Integrate scheduled job
     * Add @EnableScheduling to main application class
     * Configure cron expression: "0 0 2 * * ?" (daily at 2 AM)
     */
    @Scheduled(cron = "${subscription.renewal.cron:0 0 2 * * ?}")
    public void scheduledProcessExpiringSubscriptions() {
        log.info("Running scheduled subscription renewal check");
        try {
            processExpiringSubscriptions();
        } catch (Exception e) {
            log.error("Error in scheduled subscription renewal check: {}", e.getMessage(), e);
        }
    }

    @Override
    public void processExpiringSubscriptions() {
        log.info("Processing expiring subscriptions");
        
        // Get subscriptions expiring in next 7 days
        LocalDate currentDate = LocalDate.now();
        LocalDate expiryDate = currentDate.plusDays(7);
        
        // Get all subscriptions expiring soon (across all colleges for scheduled job)
        List<Subscription> expiringSubscriptions = subscriptionRepository
                .findExpiringSoon(currentDate, expiryDate);
        
        for (Subscription subscription : expiringSubscriptions) {
            try {
                if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                    int daysRemaining = (int) java.time.temporal.ChronoUnit.DAYS.between(
                            currentDate, subscription.getExpiresAt());
                    
                    // Send renewal reminder
                    sendRenewalReminder(subscription, daysRemaining);
                    
                    // Generate renewal invoice if expiring in 3 days
                    if (daysRemaining <= 3) {
                        try {
                            generateRenewalInvoice(subscription.getUuid());
                        } catch (Exception e) {
                            log.warn("Failed to generate renewal invoice for subscription {}: {}", 
                                    subscription.getUuid(), e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error processing subscription {}: {}", subscription.getUuid(), e.getMessage());
            }
        }
        
        log.info("Processed {} expiring subscriptions", expiringSubscriptions.size());
    }

    @Override
    public void generateRenewalInvoice(String subscriptionUuid) {
        log.info("Generating renewal invoice for subscription: {}", subscriptionUuid);
        
        try {
            // Check if renewal invoice already exists
            // For renewal, we need to find subscription without college isolation (scheduled job context)
            // Using a query that finds by UUID across all colleges
            Subscription subscription = subscriptionRepository.findAll()
                    .stream()
                    .filter(s -> s.getUuid().equals(subscriptionUuid))
                    .findFirst()
                    .orElseThrow(() -> new org.collegemanagement.exception.ResourceNotFoundException(
                            "Subscription not found: " + subscriptionUuid));
            
            // Check if unpaid invoice already exists for this subscription
            org.springframework.data.domain.Page<org.collegemanagement.dto.invoice.InvoiceResponse> unpaidInvoicesPage = 
                    invoiceService.getUnpaidInvoices(org.springframework.data.domain.Pageable.unpaged());
            
            boolean hasUnpaidInvoice = unpaidInvoicesPage.getContent().stream()
                    .anyMatch(inv -> inv.getSubscriptionUuid().equals(subscriptionUuid));
            
            if (!hasUnpaidInvoice) {
                // Generate renewal invoice
                invoiceService.generateInvoice(subscriptionUuid);
                log.info("Renewal invoice generated for subscription: {}", subscriptionUuid);
            } else {
                log.info("Renewal invoice already exists for subscription: {}", subscriptionUuid);
            }
        } catch (Exception e) {
            log.error("Failed to generate renewal invoice for subscription {}: {}", 
                    subscriptionUuid, e.getMessage());
            throw new RuntimeException("Failed to generate renewal invoice", e);
        }
    }

    @Override
    public void sendRenewalReminders(int daysBeforeExpiry) {
        log.info("Sending renewal reminders for subscriptions expiring in {} days", daysBeforeExpiry);
        
        LocalDate currentDate = LocalDate.now();
        LocalDate expiryDate = currentDate.plusDays(daysBeforeExpiry);
        
        // Get all subscriptions expiring soon (across all colleges)
        List<Subscription> expiringSubscriptions = subscriptionRepository
                .findExpiringSoon(currentDate, expiryDate);
        
        for (Subscription subscription : expiringSubscriptions) {
            if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                sendRenewalReminder(subscription, daysBeforeExpiry);
            }
        }
    }

    private void sendRenewalReminder(Subscription subscription, int daysRemaining) {
        try {
            emailService.sendSubscriptionExpiringEmail(
                    subscription.getCollege().getEmail(),
                    subscription.getCollege().getName(),
                    subscription.getPlan().getCode().name(),
                    subscription.getExpiresAt(),
                    daysRemaining
            );
            log.info("Renewal reminder sent for subscription: {}", subscription.getUuid());
        } catch (Exception e) {
            log.warn("Failed to send renewal reminder for subscription {}: {}", 
                    subscription.getUuid(), e.getMessage());
        }
    }
}

