package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.enums.AdjustmentType;
import org.collegemanagement.services.EmailService;
import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StreamUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.nio.charset.StandardCharsets;

/**
 * Email service implementation using Spring Mail.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@collegemanagement.com}")
    private String fromEmail;

    @Value("${spring.mail.properties.mail.from.name:College Management System}")
    private String fromName;

    @Override
    public void sendPaymentConfirmationEmail(String toEmail, String collegeName, String invoiceNumber, 
                                            java.math.BigDecimal amount, String transactionId) {
        try {
            String emailBody = buildPaymentConfirmationBody(collegeName, invoiceNumber, amount, transactionId);
            sendHtmlEmail(toEmail, "Payment Confirmation - Invoice " + invoiceNumber, emailBody);
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send payment confirmation email", e);
        }
    }

    @Override
    public void sendInvoiceGeneratedEmail(String toEmail, String collegeName, String invoiceNumber, 
                                         java.math.BigDecimal amount, LocalDate dueDate) {
        try {
            String emailBody = buildInvoiceGeneratedBody(collegeName, invoiceNumber, amount, dueDate);
            sendHtmlEmail(toEmail, "Invoice Generated - " + invoiceNumber, emailBody);
        } catch (Exception e) {
            log.error("Failed to send invoice generated email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send invoice generated email", e);
        }
    }

    @Override
    public void sendSubscriptionActivatedEmail(String toEmail, String collegeName, String planType, LocalDate expiresAt) {
        try {
            String emailBody = buildSubscriptionActivatedBody(collegeName, planType, expiresAt);
            sendHtmlEmail(toEmail, "Subscription Activated - " + planType, emailBody);
        } catch (Exception e) {
            log.error("Failed to send subscription activated email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send subscription activated email", e);
        }
    }

    @Override
    public void sendSubscriptionExpiringEmail(String toEmail, String collegeName, String planType, 
                                            LocalDate expiresAt, int daysRemaining) {
        try {
            String emailBody = buildSubscriptionExpiringBody(collegeName, planType, expiresAt, daysRemaining);
            sendHtmlEmail(toEmail, "Subscription Expiring Soon - " + daysRemaining + " days remaining", emailBody);
        } catch (Exception e) {
            log.error("Failed to send subscription expiring email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send subscription expiring email", e);
        }
    }

    @Override
    public void sendSubscriptionExpiredEmail(String toEmail, String collegeName, String planType, LocalDate expiredAt) {
        try {
            String emailBody = buildSubscriptionExpiredBody(collegeName, planType, expiredAt);
            sendHtmlEmail(toEmail, "Subscription Expired - Renewal Required", emailBody);
        } catch (Exception e) {
            log.error("Failed to send subscription expired email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send subscription expired email", e);
        }
    }

    @Override
    public void sendPaymentFailureEmail(String toEmail, String collegeName, String invoiceNumber, String failureReason) {
        try {
            String emailBody = buildPaymentFailureBody(collegeName, invoiceNumber, failureReason);
            sendHtmlEmail(toEmail, "Payment Failed - Invoice " + invoiceNumber, emailBody);
        } catch (Exception e) {
            log.error("Failed to send payment failure email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send payment failure email", e);
        }
    }

    @Override
    public void sendStudentFeePaymentEmail(String toEmail, String collegeName, String studentName,
                                           java.math.BigDecimal amount, String receiptNumber,
                                           String transactionId, Instant paymentDate,
                                           java.math.BigDecimal dueAmount) {
        try {
            String emailBody = buildStudentFeePaymentBody(collegeName, studentName, amount, receiptNumber, transactionId, paymentDate, dueAmount);
            sendHtmlEmail(toEmail, "Fee Payment Receipt - " + receiptNumber, emailBody);
        } catch (Exception e) {
            log.error("Failed to send fee payment email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send fee payment email", e);
        }
    }

    @Override
    public void sendStudentFeeOverdueEmail(String toEmail, String collegeName, String studentName,
                                           java.math.BigDecimal dueAmount, LocalDate dueDate) {
        try {
            String emailBody = buildStudentFeeOverdueBody(collegeName, studentName, dueAmount, dueDate);
            sendHtmlEmail(toEmail, "Fee Overdue Reminder", emailBody);
        } catch (Exception e) {
            log.error("Failed to send fee overdue email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send fee overdue email", e);
        }
    }

    @Override
    public void sendStudentFeeAdjustmentEmail(String toEmail, String collegeName, String studentName,
                                              AdjustmentType type, java.math.BigDecimal amount,
                                              java.math.BigDecimal netAmount, java.math.BigDecimal dueAmount) {
        try {
            String emailBody = buildStudentFeeAdjustmentBody(collegeName, studentName, type, amount, netAmount, dueAmount);
            sendHtmlEmail(toEmail, "Fee Adjustment Notice", emailBody);
        } catch (Exception e) {
            log.error("Failed to send fee adjustment email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send fee adjustment email", e);
        }
    }

    private void sendTextEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(String.format("%s <%s>", fromName, fromEmail));
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private void sendHtmlEmail(String toEmail, String subject, String body) throws UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    // Email body builders
    private String buildPaymentConfirmationBody(String collegeName, String invoiceNumber, 
                                               java.math.BigDecimal amount, String transactionId) {
        return renderTemplate("email/payment-confirmation.html",
                "collegeName", collegeName,
                "invoiceNumber", invoiceNumber,
                "amount", String.valueOf(amount),
                "transactionId", transactionId != null ? transactionId : "N/A");
    }

    private String buildInvoiceGeneratedBody(String collegeName, String invoiceNumber, 
                                           java.math.BigDecimal amount, LocalDate dueDate) {
        String date = dueDate != null ? dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";
        return renderTemplate("email/invoice-generated.html",
                "collegeName", collegeName,
                "invoiceNumber", invoiceNumber,
                "amount", String.valueOf(amount),
                "dueDate", date);
    }

    private String buildSubscriptionActivatedBody(String collegeName, String planType, LocalDate expiresAt) {
        String date = expiresAt != null ? expiresAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";
        return renderTemplate("email/subscription-activated.html",
                "collegeName", collegeName,
                "planType", planType,
                "expiresAt", date);
    }

    private String buildSubscriptionExpiringBody(String collegeName, String planType, 
                                                LocalDate expiresAt, int daysRemaining) {
        String date = expiresAt != null ? expiresAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";
        return renderTemplate("email/subscription-expiring.html",
                "collegeName", collegeName,
                "planType", planType,
                "expiresAt", date,
                "daysRemaining", String.valueOf(daysRemaining));
    }

    private String buildSubscriptionExpiredBody(String collegeName, String planType, LocalDate expiredAt) {
        String date = expiredAt != null ? expiredAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";
        return renderTemplate("email/subscription-expired.html",
                "collegeName", collegeName,
                "planType", planType,
                "expiredAt", date);
    }

    private String buildPaymentFailureBody(String collegeName, String invoiceNumber, String failureReason) {
        return renderTemplate("email/payment-failure.html",
                "collegeName", collegeName,
                "invoiceNumber", invoiceNumber,
                "failureReason", failureReason != null ? failureReason : "Unknown error");
    }

    private String buildStudentFeePaymentBody(String collegeName, String studentName,
                                              java.math.BigDecimal amount, String receiptNumber,
                                              String transactionId, Instant paymentDate,
                                              java.math.BigDecimal dueAmount) {
        String date = paymentDate != null
                ? paymentDate.atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "N/A";
        return renderTemplate("email/fee-payment.html",
                "collegeName", collegeName,
                "studentName", studentName,
                "receiptNumber", receiptNumber,
                "amountPaid", String.valueOf(amount),
                "transactionId", transactionId != null ? transactionId : "N/A",
                "paymentDate", date,
                "dueAmount", String.valueOf(dueAmount));
    }

    private String buildStudentFeeOverdueBody(String collegeName, String studentName,
                                              java.math.BigDecimal dueAmount, LocalDate dueDate) {
        String date = dueDate != null ? dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";
        return renderTemplate("email/fee-overdue.html",
                "collegeName", collegeName,
                "studentName", studentName,
                "dueDate", date,
                "dueAmount", String.valueOf(dueAmount));
    }

    private String buildStudentFeeAdjustmentBody(String collegeName, String studentName,
                                                 AdjustmentType type, java.math.BigDecimal amount,
                                                 java.math.BigDecimal netAmount, java.math.BigDecimal dueAmount) {
        return renderTemplate("email/fee-adjustment.html",
                "collegeName", collegeName,
                "studentName", studentName,
                "adjustmentType", type != null ? type.name() : "N/A",
                "adjustmentAmount", String.valueOf(amount),
                "netAmount", String.valueOf(netAmount),
                "dueAmount", String.valueOf(dueAmount));
    }

    private String renderTemplate(String path, String... pairs) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/" + path);
            String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            if (pairs != null) {
                for (int i = 0; i + 1 < pairs.length; i += 2) {
                    template = template.replace("{{" + pairs[i] + "}}", pairs[i + 1] != null ? pairs[i + 1] : "");
                }
            }
            return template;
        } catch (IOException e) {
            log.warn("Failed to load template {}: {}", path, e.getMessage());
            return "";
        }
    }
}

