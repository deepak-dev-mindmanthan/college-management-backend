package org.collegemanagement.services.admin;

import org.collegemanagement.dto.accountant.AccountantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminAccountantService {

    /**
     * Global accountant listing across all colleges.
     * SUPER_ADMIN only.
     */
    Page<AccountantResponse> getAllAccountantsAcrossColleges(Pageable pageable);
}
