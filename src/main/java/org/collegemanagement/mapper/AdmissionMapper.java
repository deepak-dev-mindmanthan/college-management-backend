package org.collegemanagement.mapper;

import org.collegemanagement.dto.admission.AdmissionResponse;
import org.collegemanagement.entity.admission.AdmissionApplication;

public final class AdmissionMapper {

    private AdmissionMapper() {
    }

    /**
     * Convert AdmissionApplication entity to AdmissionResponse
     */
    public static AdmissionResponse toResponse(AdmissionApplication application) {
        if (application == null) {
            return null;
        }

        return AdmissionResponse.builder()
                .uuid(application.getUuid())
                .applicationNo(application.getApplicationNo())
                .studentName(application.getStudentName())
                .dob(application.getDob())
                .gender(application.getGender())
                .email(application.getEmail())
                .phone(application.getPhone())
                .classUuid(application.getAppliedClass() != null ? application.getAppliedClass().getUuid() : null)
                .className(application.getAppliedClass() != null ? application.getAppliedClass().getName() : null)
                .section(application.getAppliedClass() != null ? application.getAppliedClass().getSection() : null)
                .previousSchool(application.getPreviousSchool())
                .documentsJson(application.getDocumentsJson())
                .status(application.getStatus())
                .submittedAt(application.getSubmittedAt())
                .collegeId(application.getCollege() != null ? application.getCollege().getId() : null)
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}

