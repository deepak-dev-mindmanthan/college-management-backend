package org.collegemanagement.services.impl.admin;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.admission.AdmissionResponse;
import org.collegemanagement.entity.admission.AdmissionApplication;
import org.collegemanagement.mapper.AdmissionMapper;
import org.collegemanagement.repositories.AdmissionApplicationRepository;
import org.collegemanagement.services.admin.AdminAdmissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAdmissionServiceImpl implements AdminAdmissionService {

    private final AdmissionApplicationRepository admissionApplicationRepository;

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public Page<AdmissionResponse> getAllAdmissions(Pageable pageable) {

        Page<AdmissionApplication> applications = admissionApplicationRepository.findAll(pageable);

        return applications.map(AdmissionMapper::toResponse);
    }
}
