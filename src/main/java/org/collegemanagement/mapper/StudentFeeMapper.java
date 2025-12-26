package org.collegemanagement.mapper;

import org.collegemanagement.dto.fees.*;
import org.collegemanagement.entity.fees.FeeComponent;
import org.collegemanagement.entity.fees.FeePayment;
import org.collegemanagement.entity.fees.FeeStructure;
import org.collegemanagement.entity.fees.StudentFee;
import org.collegemanagement.enums.FeeStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public final class StudentFeeMapper {

    private StudentFeeMapper() {
    }

    /**
     * Convert FeeComponent entity to FeeComponentResponse
     */
    public static FeeComponentResponse toComponentResponse(FeeComponent component) {
        if (component == null) {
            return null;
        }

        return FeeComponentResponse.builder()
                .uuid(component.getUuid())
                .name(component.getName())
                .amount(component.getAmount())
                .createdAt(component.getCreatedAt())
                .updatedAt(component.getUpdatedAt())
                .build();
    }

    /**
     * Convert list of FeeComponent entities to list of FeeComponentResponse
     */
    public static List<FeeComponentResponse> toComponentResponseList(List<FeeComponent> components) {
        if (components == null) {
            return List.of();
        }
        return components.stream()
                .map(StudentFeeMapper::toComponentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert FeeStructure entity to FeeStructureResponse
     */
    public static FeeStructureResponse toFeeStructureResponse(FeeStructure feeStructure) {
        if (feeStructure == null) {
            return null;
        }

        List<FeeComponent> components = feeStructure.getComponents() != null ?
                feeStructure.getComponents().stream().toList() : List.of();

        return FeeStructureResponse.builder()
                .uuid(feeStructure.getUuid())
                .classUuid(feeStructure.getClassRoom() != null ? feeStructure.getClassRoom().getUuid() : null)
                .className(feeStructure.getClassRoom() != null ? feeStructure.getClassRoom().getName() : null)
                .section(feeStructure.getClassRoom() != null ? feeStructure.getClassRoom().getSection() : null)
                .totalAmount(feeStructure.getTotalAmount())
                .components(toComponentResponseList(components))
                .createdAt(feeStructure.getCreatedAt())
                .updatedAt(feeStructure.getUpdatedAt())
                .build();
    }

    /**
     * Convert StudentFee entity to StudentFeeResponse
     */
    public static StudentFeeResponse toStudentFeeResponse(StudentFee studentFee) {
        if (studentFee == null) {
            return null;
        }

        var student = studentFee.getStudent();
        var feeStructure = studentFee.getFeeStructure();

        return StudentFeeResponse.builder()
                .uuid(studentFee.getUuid())
                .studentUuid(student != null ? student.getUuid() : null)
                .studentName(student != null && student.getUser() != null ? student.getUser().getName() : null)
                .rollNumber(student != null ? student.getRollNumber() : null)
                .feeStructureUuid(feeStructure != null ? feeStructure.getUuid() : null)
                .className(feeStructure != null && feeStructure.getClassRoom() != null ?
                        feeStructure.getClassRoom().getName() : null)
                .section(feeStructure != null && feeStructure.getClassRoom() != null ?
                        feeStructure.getClassRoom().getSection() : null)
                .totalAmount(studentFee.getTotalAmount())
                .paidAmount(studentFee.getPaidAmount())
                .dueAmount(studentFee.getDueAmount())
                .status(studentFee.getStatus())
                .createdAt(studentFee.getCreatedAt())
                .updatedAt(studentFee.getUpdatedAt())
                .build();
    }

    /**
     * Convert list of StudentFee entities to list of StudentFeeResponse
     */
    public static List<StudentFeeResponse> toStudentFeeResponseList(List<StudentFee> studentFees) {
        if (studentFees == null) {
            return List.of();
        }
        return studentFees.stream()
                .map(StudentFeeMapper::toStudentFeeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert FeePayment entity to FeePaymentResponse
     */
    public static FeePaymentResponse toFeePaymentResponse(FeePayment feePayment) {
        if (feePayment == null) {
            return null;
        }

        var studentFee = feePayment.getStudentFee();
        var student = studentFee != null ? studentFee.getStudent() : null;

        return FeePaymentResponse.builder()
                .uuid(feePayment.getUuid())
                .studentFeeUuid(studentFee != null ? studentFee.getUuid() : null)
                .studentUuid(student != null ? student.getUuid() : null)
                .studentName(student != null && student.getUser() != null ? student.getUser().getName() : null)
                .amount(feePayment.getAmount())
                .paymentMode(feePayment.getPaymentMode())
                .transactionId(feePayment.getTransactionId())
                .paymentDate(feePayment.getPaymentDate())
                .createdAt(feePayment.getCreatedAt())
                .updatedAt(feePayment.getUpdatedAt())
                .build();
    }

    /**
     * Convert list of FeePayment entities to list of FeePaymentResponse
     */
    public static List<FeePaymentResponse> toFeePaymentResponseList(List<FeePayment> feePayments) {
        if (feePayments == null) {
            return List.of();
        }
        return feePayments.stream()
                .map(StudentFeeMapper::toFeePaymentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Calculate fee status based on paid and due amounts
     */
    public static FeeStatus calculateFeeStatus(BigDecimal totalAmount, BigDecimal paidAmount, BigDecimal dueAmount) {
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        if (dueAmount == null) {
            dueAmount = totalAmount;
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return FeeStatus.PENDING;
        }

        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            return FeeStatus.PENDING;
        } else if (paidAmount.compareTo(totalAmount) >= 0) {
            return FeeStatus.PAID;
        } else if (dueAmount.compareTo(BigDecimal.ZERO) > 0) {
            // Check if overdue (can be enhanced with due date logic)
            return FeeStatus.PARTIALLY_PAID;
        } else {
            return FeeStatus.PARTIALLY_PAID;
        }
    }
}

