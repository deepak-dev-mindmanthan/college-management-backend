package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.enums.FeeStatus;
import org.collegemanagement.enums.InstallmentStatus;
import org.collegemanagement.entity.fees.StudentFee;
import org.collegemanagement.repositories.FeeInstallmentRepository;
import org.collegemanagement.repositories.StudentFeeRepository;
import org.collegemanagement.services.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeOverdueScheduler {

    private final StudentFeeRepository studentFeeRepository;
    private final FeeInstallmentRepository feeInstallmentRepository;
    private final EmailService emailService;

    @Value("${fees.overdue.reminder-hours:24}")
    private long reminderHours;

    @Scheduled(cron = "${fees.overdue.cron:0 30 1 * * ?}")
    @Transactional
    public void markOverdueFees() {
        LocalDate today = LocalDate.now();
        int feesUpdated = studentFeeRepository.markOverdueByDueDate(FeeStatus.OVERDUE, today);
        int installmentsUpdated = feeInstallmentRepository.markOverdue(InstallmentStatus.OVERDUE, today);

        sendOverdueReminders(today);

        if (feesUpdated > 0 || installmentsUpdated > 0) {
            log.info("Marked overdue fees: {}, overdue installments: {}", feesUpdated, installmentsUpdated);
        }
    }

    private void sendOverdueReminders(LocalDate today) {
        Instant cutoff = Instant.now().minus(reminderHours, ChronoUnit.HOURS);
        List<StudentFee> overdueFees = studentFeeRepository.findOverdueForReminder(today, cutoff);
        for (StudentFee studentFee : overdueFees) {
            if (studentFee.getStudent() == null || studentFee.getStudent().getUser() == null) {
                continue;
            }
            String email = studentFee.getStudent().getUser().getEmail();
            if (email == null || email.isBlank()) {
                continue;
            }
            String collegeName = studentFee.getStudent().getCollege() != null ? studentFee.getStudent().getCollege().getName() : "College";
            String studentName = studentFee.getStudent().getUser().getName();
            try {
                emailService.sendStudentFeeOverdueEmail(
                        email,
                        collegeName,
                        studentName != null ? studentName : "Student",
                        studentFee.getDueAmount(),
                        studentFee.getDueDate()
                );
                studentFee.setLastOverdueNotifiedAt(Instant.now());
                studentFeeRepository.save(studentFee);
            } catch (Exception e) {
                log.warn("Failed to send overdue reminder to {}: {}", email, e.getMessage());
            }
        }
    }
}
