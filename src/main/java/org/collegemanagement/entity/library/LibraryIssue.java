package org.collegemanagement.entity.library;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.enums.LibraryIssueStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "library_issues",
        indexes = {
                @Index(name = "idx_library_issue_book", columnList = "book_id"),
                @Index(name = "idx_library_issue_user", columnList = "issued_to_user_id"),
                @Index(name = "idx_library_issue_issued_by", columnList = "issued_by"),
                @Index(name = "idx_library_issue_due", columnList = "due_date"),
                @Index(name = "idx_library_issue_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LibraryIssue extends BaseEntity {

    /**
     * Issued book
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private LibraryBook book;

    /**
     * User who borrowed the book (Student / Staff)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issued_to_user_id", nullable = false)
    private User issuedTo;

    /**
     * User who issued the book (Librarian / Admin)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issued_by", nullable = false)
    private User issuedBy;

    /**
     * Issue date
     */
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    /**
     * Due date
     */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * Actual return date
     */
    @Column(name = "return_date")
    private LocalDate returnDate;

    /**
     * Fine amount if applicable
     */
    @Column(name = "fine_amount")
    private BigDecimal fineAmount;

    /**
     * Issue lifecycle status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LibraryIssueStatus status;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = LibraryIssueStatus.ISSUED;
        }
    }
}
