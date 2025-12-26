package org.collegemanagement.services;

import org.collegemanagement.dto.admission.*;
import org.collegemanagement.dto.student.StudentResponse;
import org.collegemanagement.enums.AdmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdmissionService {

    /**
     * Create a new admission application (DRAFT status)
     */
    AdmissionResponse createAdmission(CreateAdmissionRequest request);

    /**
     * Update admission application (only if in DRAFT status)
     */
    AdmissionResponse updateAdmission(String admissionUuid, UpdateAdmissionRequest request);

    /**
     * Get admission application by UUID
     */
    AdmissionResponse getAdmissionByUuid(String admissionUuid);

    /**
     * Get all admission applications with pagination
     */
    Page<AdmissionResponse> getAllAdmissions(Pageable pageable);

    /**
     * Search admission applications by student name, email, phone, or application number
     */
    Page<AdmissionResponse> searchAdmissions(String searchTerm, Pageable pageable);

    /**
     * Get admission applications by status
     */
    Page<AdmissionResponse> getAdmissionsByStatus(AdmissionStatus status, Pageable pageable);

    /**
     * Get admission applications by class UUID
     */
    Page<AdmissionResponse> getAdmissionsByClass(String classUuid, Pageable pageable);

    /**
     * Submit admission application (DRAFT → SUBMITTED)
     */
    AdmissionResponse submitAdmission(String admissionUuid);

    /**
     * Verify admission application (SUBMITTED → VERIFIED)
     */
    AdmissionResponse verifyAdmission(String admissionUuid);

    /**
     * Approve admission application (VERIFIED → APPROVED) and create Student
     */
    StudentResponse approveAdmission(String admissionUuid, ApproveAdmissionRequest request);

    /**
     * Reject admission application (any status → REJECTED)
     */
    AdmissionResponse rejectAdmission(String admissionUuid);

    /**
     * Delete admission application (only if in DRAFT status)
     */
    void deleteAdmission(String admissionUuid);

    /**
     * Get admission summary statistics
     */
    AdmissionSummary getAdmissionSummary();
}

