package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.library.*;
import org.collegemanagement.entity.library.LibraryBook;
import org.collegemanagement.entity.library.LibraryIssue;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.LibraryIssueStatus;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.LibraryMapper;
import org.collegemanagement.repositories.LibraryBookRepository;
import org.collegemanagement.repositories.LibraryIssueRepository;
import org.collegemanagement.repositories.UserRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.LibraryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryServiceImpl implements LibraryService {

    private final LibraryBookRepository libraryBookRepository;
    private final LibraryIssueRepository libraryIssueRepository;
    private final UserRepository userRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;

    // Fine calculation constants (could be moved to configuration)
    private static final BigDecimal FINE_PER_DAY = BigDecimal.valueOf(10.0); // 10 currency units per day

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN')")
    public BookResponse createBook(CreateBookRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate ISBN uniqueness within college if provided
        if (request.getIsbn() != null && !request.getIsbn().isBlank()) {
            if (libraryBookRepository.existsByIsbnAndCollegeId(request.getIsbn(), collegeId)) {
                throw new ResourceConflictException("Book with ISBN " + request.getIsbn() + " already exists in this college");
            }
        }

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Create book
        LibraryBook book = LibraryBook.builder()
                .college(college)
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .publisher(request.getPublisher())
                .category(request.getCategory())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies()) // Initially all copies are available
                .build();

        book = libraryBookRepository.save(book);

        return LibraryMapper.toBookResponse(book);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN')")
    public BookResponse updateBook(String bookUuid, UpdateBookRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find book
        LibraryBook book = libraryBookRepository.findByUuidAndCollegeId(bookUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with UUID: " + bookUuid));

        // Update ISBN if provided and validate uniqueness
        if (request.getIsbn() != null && !request.getIsbn().equals(book.getIsbn())) {
            if (!request.getIsbn().isBlank() && libraryBookRepository.existsByIsbnAndCollegeIdAndIdNot(request.getIsbn(), collegeId, book.getId())) {
                throw new ResourceConflictException("Book with ISBN " + request.getIsbn() + " already exists in this college");
            }
            book.setIsbn(request.getIsbn());
        }

        // Update other fields if provided
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            book.setTitle(request.getTitle());
        }
        if (request.getAuthor() != null && !request.getAuthor().isBlank()) {
            book.setAuthor(request.getAuthor());
        }
        if (request.getPublisher() != null) {
            book.setPublisher(request.getPublisher());
        }
        if (request.getCategory() != null) {
            book.setCategory(request.getCategory());
        }
        if (request.getTotalCopies() != null) {
            int currentIssuedCopies = book.getTotalCopies() - book.getAvailableCopies();
            int newTotalCopies = request.getTotalCopies();

            if (newTotalCopies < currentIssuedCopies) {
                throw new ResourceConflictException("Cannot set total copies to " + newTotalCopies +
                        ". There are " + currentIssuedCopies + " copies currently issued.");
            }

            book.setTotalCopies(newTotalCopies);
            book.setAvailableCopies(newTotalCopies - currentIssuedCopies);
        }

        book = libraryBookRepository.save(book);

        return LibraryMapper.toBookResponse(book);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public BookResponse getBookByUuid(String bookUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        LibraryBook book = libraryBookRepository.findByUuidAndCollegeId(bookUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with UUID: " + bookUuid));

        return LibraryMapper.toBookResponse(book);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<LibraryBook> books = libraryBookRepository.findAllByCollegeId(collegeId, pageable);

        return books.map(LibraryMapper::toBookResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public Page<BookResponse> searchBooks(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<LibraryBook> books = libraryBookRepository.searchBooksByCollegeId(collegeId, searchTerm, pageable);

        return books.map(LibraryMapper::toBookResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public Page<BookResponse> getBooksByCategory(String category, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<LibraryBook> books = libraryBookRepository.findByCategoryAndCollegeId(collegeId, category, pageable);

        return books.map(LibraryMapper::toBookResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public Page<BookResponse> getAvailableBooks(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<LibraryBook> books = libraryBookRepository.findAvailableBooksByCollegeId(collegeId, pageable);

        return books.map(LibraryMapper::toBookResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN')")
    public void deleteBook(String bookUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        LibraryBook book = libraryBookRepository.findByUuidAndCollegeId(bookUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with UUID: " + bookUuid));

        // Check if there are any active issues for this book
        Page<LibraryIssue> activeIssuesPage = libraryIssueRepository.findByStatusAndCollegeId(
                LibraryIssueStatus.ISSUED, collegeId, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE));
        List<LibraryIssue> activeIssues = activeIssuesPage.getContent().stream()
                .filter(issue -> issue.getBook().getId().equals(book.getId()))
                .toList();

        if (!activeIssues.isEmpty()) {
            throw new ResourceConflictException("Cannot delete book. There are " + activeIssues.size() +
                    " active issues. Please return all books first.");
        }

        libraryBookRepository.delete(book);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER')")
    public LibraryIssueResponse issueBook(IssueBookRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find book
        LibraryBook book = libraryBookRepository.findByUuidAndCollegeId(request.getBookUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with UUID: " + request.getBookUuid()));

        // Check availability
        if (book.getAvailableCopies() <= 0) {
            throw new ResourceConflictException("No copies available for this book");
        }

        // Find user and validate tenant access
        User user = findUserByUuid(request.getUserUuid());

        // Enforce tenant isolation using TenantAccessGuard
        if (user.getCollege() != null) {
            tenantAccessGuard.assertCurrentTenant(user.getCollege());
        } else {
            // Users without college are not allowed (except SUPER_ADMIN, but they shouldn't borrow books)
            throw new ResourceConflictException("User does not belong to a college");
        }

        // Check if user already has an active issue for this book
        if (libraryIssueRepository.existsActiveIssueByBookIdAndUserId(book.getId(), user.getId(), LibraryIssueStatus.ISSUED)) {
            throw new ResourceConflictException("User already has an active issue for this book");
        }

        // Validate due date
        if (request.getDueDate().isBefore(LocalDate.now())) {
            throw new ResourceConflictException("Due date cannot be in the past");
        }

        // Get current user (librarian/admin who is issuing)
        User issuedBy = getCurrentUser();

        // Create issue
        LibraryIssue issue = LibraryIssue.builder()
                .book(book)
                .issuedTo(user)
                .issuedBy(issuedBy)
                .issueDate(LocalDate.now())
                .dueDate(request.getDueDate())
                .status(LibraryIssueStatus.ISSUED)
                .build();

        issue = libraryIssueRepository.save(issue);

        // Update book availability
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        libraryBookRepository.save(book);

        return LibraryMapper.toIssueResponse(issue);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER')")
    public LibraryIssueResponse returnBook(String issueUuid, ReturnBookRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find issue
        LibraryIssue issue = libraryIssueRepository.findByUuidAndCollegeId(issueUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with UUID: " + issueUuid));

        // Check if already returned
        if (issue.getStatus() == LibraryIssueStatus.RETURNED) {
            throw new ResourceConflictException("Book has already been returned");
        }

        // Calculate fine if overdue
        BigDecimal fineAmount = request.getFineAmount();
        if (fineAmount == null && issue.getDueDate().isBefore(LocalDate.now())) {
            long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(issue.getDueDate(), LocalDate.now());
            fineAmount = FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays));
        }

        // Update issue
        issue.setReturnDate(LocalDate.now());
        issue.setStatus(LibraryIssueStatus.RETURNED);
        if (fineAmount != null) {
            issue.setFineAmount(fineAmount);
        }

        issue = libraryIssueRepository.save(issue);

        // Update book availability
        LibraryBook book = issue.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        libraryBookRepository.save(book);

        return LibraryMapper.toIssueResponse(issue);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public LibraryIssueResponse getIssueByUuid(String issueUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        LibraryIssue issue = libraryIssueRepository.findByUuidAndCollegeId(issueUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with UUID: " + issueUuid));

        return LibraryMapper.toIssueResponse(issue);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER')")
    public Page<LibraryIssueResponse> getAllIssues(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<LibraryIssue> issues = libraryIssueRepository.findAllByCollegeId(collegeId, pageable);

        return issues.map(LibraryMapper::toIssueResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER')")
    public Page<LibraryIssueResponse> getIssuesByStatus(LibraryIssueStatus status, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<LibraryIssue> issues = libraryIssueRepository.findByStatusAndCollegeId(status, collegeId, pageable);

        return issues.map(LibraryMapper::toIssueResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public Page<LibraryIssueResponse> getIssuesByUser(String userUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find user and validate tenant access
        User user = findUserByUuid(userUuid);

        // Enforce tenant isolation using TenantAccessGuard
        if (user.getCollege() != null) {
            tenantAccessGuard.assertCurrentTenant(user.getCollege());
        } else {
            throw new ResourceConflictException("User does not belong to a college");
        }

        // Allow users to view only their own issues (or if admin/teacher/librarian)
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(user.getId()) &&
                !isAdminOrTeacherOrLibrarian(currentUser)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }

        Page<LibraryIssue> issues = libraryIssueRepository.findByUserIdAndCollegeId(user.getId(), collegeId, pageable);

        return issues.map(LibraryMapper::toIssueResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public Page<LibraryIssueResponse> getActiveIssuesByUser(String userUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find user and validate tenant access
        User user = findUserByUuid(userUuid);

        // Enforce tenant isolation using TenantAccessGuard
        if (user.getCollege() != null) {
            tenantAccessGuard.assertCurrentTenant(user.getCollege());
        } else {
            throw new ResourceConflictException("User does not belong to a college");
        }

        // Allow users to view only their own issues (or if admin/teacher/librarian)
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(user.getId()) &&
                !isAdminOrTeacherOrLibrarian(currentUser)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }

        List<LibraryIssue> activeIssues = libraryIssueRepository.findActiveIssuesByUserIdAndCollegeId(
                user.getId(), LibraryIssueStatus.ISSUED, collegeId);

        // Convert to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), activeIssues.size());
        List<LibraryIssue> pagedIssues = activeIssues.subList(start, end);

        List<LibraryIssueResponse> responses = pagedIssues.stream()
                .map(LibraryMapper::toIssueResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, activeIssues.size());
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER')")
    public Page<LibraryIssueResponse> getOverdueIssues(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<LibraryIssue> issues = libraryIssueRepository.findOverdueIssuesByCollegeId(
                LibraryIssueStatus.ISSUED, LocalDate.now(), collegeId, pageable);

        return issues.map(LibraryMapper::toIssueResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public Page<LibraryIssueResponse> getOverdueIssuesByUser(String userUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find user and validate tenant access
        User user = findUserByUuid(userUuid);

        // Enforce tenant isolation using TenantAccessGuard
        if (user.getCollege() != null) {
            tenantAccessGuard.assertCurrentTenant(user.getCollege());
        } else {
            throw new ResourceConflictException("User does not belong to a college");
        }

        // Allow users to view only their own issues (or if admin/teacher/librarian)
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(user.getId()) &&
                !isAdminOrTeacherOrLibrarian(currentUser)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }

        List<LibraryIssue> overdueIssues = libraryIssueRepository.findOverdueIssuesByUserIdAndCollegeId(
                user.getId(), LibraryIssueStatus.ISSUED, LocalDate.now(), collegeId);

        // Convert to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), overdueIssues.size());
        List<LibraryIssue> pagedIssues = overdueIssues.subList(start, end);

        List<LibraryIssueResponse> responses = pagedIssues.stream()
                .map(LibraryMapper::toIssueResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, overdueIssues.size());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public LibraryIssueResponse updateIssueStatus(String issueUuid, LibraryIssueStatus status) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        LibraryIssue issue = libraryIssueRepository.findByUuidAndCollegeId(issueUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with UUID: " + issueUuid));

        // Validate status transition
        if (issue.getStatus() == LibraryIssueStatus.RETURNED && status != LibraryIssueStatus.RETURNED) {
            throw new ResourceConflictException("Cannot change status from RETURNED");
        }

        issue.setStatus(status);

        // If marking as OVERDUE, calculate fine
        if (status == LibraryIssueStatus.OVERDUE && issue.getDueDate().isBefore(LocalDate.now())) {
            long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(issue.getDueDate(), LocalDate.now());
            BigDecimal fineAmount = FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays));
            issue.setFineAmount(fineAmount);
        }

        issue = libraryIssueRepository.save(issue);

        return LibraryMapper.toIssueResponse(issue);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public BigDecimal calculateFine(String issueUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        LibraryIssue issue = libraryIssueRepository.findByUuidAndCollegeId(issueUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with UUID: " + issueUuid));

        // If already returned, return the fine amount
        if (issue.getStatus() == LibraryIssueStatus.RETURNED) {
            return issue.getFineAmount() != null ? issue.getFineAmount() : BigDecimal.ZERO;
        }

        // Calculate fine if overdue
        if (issue.getDueDate().isBefore(LocalDate.now())) {
            long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(issue.getDueDate(), LocalDate.now());
            return FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays));
        }

        return BigDecimal.ZERO;
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER')")
    public LibrarySummary getLibrarySummary() {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        long totalBooks = libraryBookRepository.countByCollegeId(collegeId);
        long totalCopies = libraryBookRepository.countTotalCopiesByCollegeId(collegeId);
        long availableCopies = libraryBookRepository.countAvailableCopiesByCollegeId(collegeId);
        long issuedBooks = libraryIssueRepository.countByStatusAndCollegeId(LibraryIssueStatus.ISSUED, collegeId);
        // Count overdue books - get first page to get total count
        Page<LibraryIssue> overduePage = libraryIssueRepository.findOverdueIssuesByCollegeId(
                LibraryIssueStatus.ISSUED, LocalDate.now(), collegeId,
                org.springframework.data.domain.PageRequest.of(0, 1));
        long overdueBooks = overduePage.getTotalElements();
        BigDecimal totalFines = libraryIssueRepository.sumFinesByCollegeId(collegeId);
        BigDecimal pendingFines = libraryIssueRepository.sumPendingFinesByCollegeId(
                collegeId, Arrays.asList(LibraryIssueStatus.ISSUED, LibraryIssueStatus.OVERDUE));

        return LibrarySummary.builder()
                .totalBooks(totalBooks)
                .totalCopies(totalCopies)
                .availableCopies(availableCopies)
                .issuedBooks(issuedBooks)
                .overdueBooks(overdueBooks)
                .totalFines(totalFines != null ? totalFines : BigDecimal.ZERO)
                .pendingFines(pendingFines != null ? pendingFines : BigDecimal.ZERO)
                .build();
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new org.springframework.security.access.AccessDeniedException("User not authenticated");
    }

    private boolean isAdminOrTeacherOrLibrarian(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_SUPER_ADMIN") ||
                                 role.getName().name().equals("ROLE_COLLEGE_ADMIN") ||
                                 role.getName().name().equals("ROLE_TEACHER") ||
                                 role.getName().name().equals("ROLE_LIBRARIAN"));
    }

    private User findUserByUuid(String userUuid) {
        return userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + userUuid));
    }
}

