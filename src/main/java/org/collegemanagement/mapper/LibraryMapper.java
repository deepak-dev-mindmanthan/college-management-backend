package org.collegemanagement.mapper;

import org.collegemanagement.dto.library.BookResponse;
import org.collegemanagement.dto.library.LibraryIssueResponse;
import org.collegemanagement.entity.library.LibraryBook;
import org.collegemanagement.entity.library.LibraryIssue;

public final class LibraryMapper {

    private LibraryMapper() {
    }

    /**
     * Convert LibraryBook entity to BookResponse
     */
    public static BookResponse toBookResponse(LibraryBook book) {
        if (book == null) {
            return null;
        }

        return BookResponse.builder()
                .uuid(book.getUuid())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .category(book.getCategory())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .collegeId(book.getCollege() != null ? book.getCollege().getId() : null)
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    /**
     * Convert LibraryIssue entity to LibraryIssueResponse
     */
    public static LibraryIssueResponse toIssueResponse(LibraryIssue issue) {
        if (issue == null) {
            return null;
        }

        return LibraryIssueResponse.builder()
                .uuid(issue.getUuid())
                .bookUuid(issue.getBook() != null ? issue.getBook().getUuid() : null)
                .bookTitle(issue.getBook() != null ? issue.getBook().getTitle() : null)
                .bookAuthor(issue.getBook() != null ? issue.getBook().getAuthor() : null)
                .bookIsbn(issue.getBook() != null ? issue.getBook().getIsbn() : null)
                .issuedToUserUuid(issue.getIssuedTo() != null ? issue.getIssuedTo().getUuid() : null)
                .issuedToUserName(issue.getIssuedTo() != null ? issue.getIssuedTo().getName() : null)
                .issuedToUserEmail(issue.getIssuedTo() != null ? issue.getIssuedTo().getEmail() : null)
                .issuedByUserUuid(issue.getIssuedBy() != null ? issue.getIssuedBy().getUuid() : null)
                .issuedByUserName(issue.getIssuedBy() != null ? issue.getIssuedBy().getName() : null)
                .issueDate(issue.getIssueDate())
                .dueDate(issue.getDueDate())
                .returnDate(issue.getReturnDate())
                .fineAmount(issue.getFineAmount())
                .status(issue.getStatus())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .build();
    }
}

