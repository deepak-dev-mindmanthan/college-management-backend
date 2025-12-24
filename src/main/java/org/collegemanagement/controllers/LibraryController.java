package org.collegemanagement.controllers;

import org.collegemanagement.dto.BookRequest;
import org.collegemanagement.dto.IssueBookRequest;
import org.collegemanagement.entity.library.LibraryBook;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.services.BookService;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.UserManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('COLLEGE_ADMIN','SUPER_ADMIN')")
@RequestMapping("/api/v1/library")
public class LibraryController {

    private final BookService bookService;
    private final CollegeService collegeService;
    private final UserManager userManager;

    public LibraryController(BookService bookService,  CollegeService collegeService, UserManager userManager) {
        this.bookService = bookService;
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
    public ResponseEntity<LibraryBook> createBook(@RequestBody BookRequest request) {
        if (!collegeService.existsById(request.getCollegeId())) {
            return ResponseEntity.badRequest().build();
        }
        requireSameCollege(request.getCollegeId());
        LibraryBook libraryBook = LibraryBook.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies())
                .college(collegeService.findById(request.getCollegeId()))
                .build();
        return ResponseEntity.ok(bookService.create(libraryBook));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<LibraryBook> updateBook(@PathVariable Long id, @RequestBody BookRequest request) {
        LibraryBook existing = bookService.findById(id);
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
        LibraryBook existing = bookService.findById(id);
        requireSameCollege(existing.getCollege().getId());
        bookService.delete(id);
        return ResponseEntity.ok("Book deleted successfully.");
    }

    @GetMapping("/books")
    public ResponseEntity<List<LibraryBook>> getBooks(@RequestParam Long collegeId) {
        requireSameCollege(collegeId);
        return ResponseEntity.ok(bookService.findByCollege(collegeId));
    }

    // Loans
    @PostMapping("/loans/issue")
    public ResponseEntity<?> issueBook(@RequestBody IssueBookRequest request) {

        return ResponseEntity.ok(null);
    }

    @PutMapping("/loans/{id}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(null);
    }
}

