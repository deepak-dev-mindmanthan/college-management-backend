package org.collegemanagement.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.LibraryIssueStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryIssueResponse {

    private String uuid;
    private String bookUuid;
    private String bookTitle;
    private String bookAuthor;
    private String bookIsbn;
    private String issuedToUserUuid;
    private String issuedToUserName;
    private String issuedToUserEmail;
    private String issuedByUserUuid;
    private String issuedByUserName;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BigDecimal fineAmount;
    private LibraryIssueStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

