package org.collegemanagement.services;

import org.collegemanagement.dto.accountant.AccountantResponse;
import org.collegemanagement.dto.accountant.CreateAccountantRequest;
import org.collegemanagement.dto.accountant.UpdateAccountantRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountantService {

    /**
     * Create a new accountant
     */
    AccountantResponse createAccountant(CreateAccountantRequest request);

    /**
     * Update accountant information
     */
    AccountantResponse updateAccountant(String accountantUuid, UpdateAccountantRequest request);

    /**
     * Get accountant by UUID
     */
    AccountantResponse getAccountantByUuid(String accountantUuid);

    /**
     * Get all accountants with pagination
     */
    Page<AccountantResponse> getAllAccountants(Pageable pageable);

    /**
     * Search accountants by name or email
     */
    Page<AccountantResponse> searchAccountants(String searchTerm, Pageable pageable);

    /**
     * Delete accountant by UUID
     */
    void deleteAccountant(String accountantUuid);
}

