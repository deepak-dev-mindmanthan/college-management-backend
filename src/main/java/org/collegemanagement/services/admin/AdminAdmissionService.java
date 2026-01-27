package org.collegemanagement.services.admin;

import org.collegemanagement.dto.admission.AdmissionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminAdmissionService {
    Page<AdmissionResponse> getAllAdmissions(Pageable pageable);
}
