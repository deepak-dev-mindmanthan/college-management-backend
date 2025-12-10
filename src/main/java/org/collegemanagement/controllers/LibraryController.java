package org.collegemanagement.controllers;

import org.collegemanagement.dto.BookRequest;
import org.collegemanagement.dto.IssueBookRequest;
import org.collegemanagement.dto.ReturnBookRequest;
import org.collegemanagement.entity.Book;
import org.collegemanagement.entity.Loan;
import org.collegemanagement.entity.User;
import org.collegemanagement.enums.LoanStatus;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.services.BookService;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.LoanService;
import org.collegemanagement.services.UserManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('COLLEGE_ADMIN','SUPER_ADMIN')")
@RequestMapping("/api/v1/library")
public class LibraryController {

    private final BookService bookService;
    private final LoanService loanService;
    private final CollegeService collegeService;
    private final UserManager userManager;

    public LibraryController(BookService bookService, LoanService loanService, CollegeService collegeService, UserManager userManager) {
        this.bookService = bookService;
        this.loanService = loanService;
        this.collegeService = collegeService;
        this.userManager = userManager;
    }

    private Authentication currentAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isSuperAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream().anyMatch(a ->
                "ROLE_SUPER_ADMIN".equalsIgnoreCase(a.getAuthority()) || "SUPER_ADMIN".equalsIgnoreCase(a.getAuthority()));
    }

    private Long currentCollegeId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) return null;
        if (authentication.getPrincipal() instanceof User user && user.getCollege() != null) {
            return user.getCollege().getId();
        }
        return null;
    }

    private void requireSameCollege(Long targetCollegeId) {
        Authentication auth = currentAuth();
        if (isSuperAdmin(auth)) {
            return;
        }
        Long currentCollege = currentCollegeId(auth);
        if (currentCollege == null || targetCollegeId == null || !currentCollege.equals(targetCollegeId)) {
            throw new AccessDeniedException("Access denied: cross-college access is not permitted.");
        }
    }

    private void requireSameCollege(User user) {
        if (user == null || user.getCollege() == null) {
            throw new AccessDeniedException("Access denied: user not assigned to a college.");
        }
        requireSameCollege(user.getCollege().getId());
    }

    // Books
    @PostMapping("/books")
    public ResponseEntity<Book> createBook(@RequestBody BookRequest request) {
        if (!collegeService.existsById(request.getCollegeId())) {
            return ResponseEntity.badRequest().build();
        }
        requireSameCollege(request.getCollegeId());
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies())
                .college(collegeService.findById(request.getCollegeId()))
                .build();
        return ResponseEntity.ok(bookService.create(book));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody BookRequest request) {
        Book existing = bookService.findById(id);
        requireSameCollege(existing.getCollege().getId());
        if (request.getTitle() != null) existing.setTitle(request.getTitle());
        if (request.getAuthor() != null) existing.setAuthor(request.getAuthor());
        if (request.getIsbn() != null) existing.setIsbn(request.getIsbn());
        if (request.getTotalCopies() != null) {
            int delta = request.getTotalCopies() - existing.getTotalCopies();
            existing.setTotalCopies(request.getTotalCopies());
            existing.setAvailableCopies(Math.max(0, existing.getAvailableCopies() + delta));
        }
        return ResponseEntity.ok(bookService.update(existing));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        Book existing = bookService.findById(id);
        requireSameCollege(existing.getCollege().getId());
        bookService.delete(id);
        return ResponseEntity.ok("Book deleted successfully.");
    }

    @GetMapping("/books")
    public ResponseEntity<List<Book>> getBooks(@RequestParam Long collegeId) {
        requireSameCollege(collegeId);
        return ResponseEntity.ok(bookService.findByCollege(collegeId));
    }

    // Loans
    @PostMapping("/loans/issue")
    public ResponseEntity<?> issueBook(@RequestBody IssueBookRequest request) {
        Book book = bookService.findById(request.getBookId());
        User student = userManager.findById(request.getStudentId());
        requireSameCollege(book.getCollege().getId());
        requireSameCollege(student);

        if (book.getAvailableCopies() <= 0) {
            return ResponseEntity.badRequest().body("No available copies for this book.");
        }

        LocalDate due = request.getDueDate() != null ? request.getDueDate() : LocalDate.now().plusDays(14);
        Loan loan = Loan.builder()
                .book(book)
                .student(student)
                .issuedAt(LocalDateTime.now())
                .dueDate(due)
                .status(LoanStatus.ISSUED)
                .build();
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookService.update(book);
        return ResponseEntity.ok(loanService.create(loan));
    }

    @PutMapping("/loans/{id}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        Loan loan = loanService.findById(id);
        requireSameCollege(loan.getBook().getCollege().getId());
        if (loan.getStatus() == LoanStatus.RETURNED) {
            return ResponseEntity.badRequest().body("Loan already returned.");
        }
        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnedAt(LocalDateTime.now());
        Loan saved = loanService.update(loan);
        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookService.update(book);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/loans/by-student")
    public ResponseEntity<List<Loan>> getLoansByStudent(@RequestParam Long studentId) {
        User student = userManager.findById(studentId);
        requireSameCollege(student);
        return ResponseEntity.ok(loanService.findByStudent(studentId));
    }

    @GetMapping("/loans/by-college")
    public ResponseEntity<List<Loan>> getLoansByCollege(@RequestParam Long collegeId,
                                                        @RequestParam(required = false) LoanStatus status) {
        requireSameCollege(collegeId);
        if (status != null) {
            return ResponseEntity.ok(loanService.findByCollegeAndStatus(collegeId, status));
        }
        return ResponseEntity.ok(loanService.findByCollege(collegeId));
    }
}

