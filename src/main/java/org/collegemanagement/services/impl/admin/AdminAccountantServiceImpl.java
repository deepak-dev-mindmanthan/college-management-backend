package org.collegemanagement.services.impl.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.accountant.AccountantResponse;
import org.collegemanagement.entity.staff.StaffProfile;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.mapper.AccountantMapper;
import org.collegemanagement.repositories.AccountantRepository;
import org.collegemanagement.repositories.StaffProfileRepository;
import org.collegemanagement.services.admin.AdminAccountantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAccountantServiceImpl implements AdminAccountantService {

    private final AccountantRepository accountantRepository;
    private final StaffProfileRepository staffProfileRepository;


    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @Override
    public Page<AccountantResponse> getAllAccountantsAcrossColleges(Pageable pageable) {

        Page<User> accountants = accountantRepository.findAllAccountants(pageable);

        return accountants.map(user -> {
            StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                    .orElse(null);
            return AccountantMapper.toResponse(user, staffProfile);
        });
    }
}
