package org.collegemanagement.entity.library;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.base.BaseEntity;


import lombok.*;

import java.util.Set;

@Entity
@Table(
        name = "library_books",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_book_isbn_per_college",
                        columnNames = {"college_id", "isbn"}
                )
        },
        indexes = {
                @Index(name = "idx_library_book_college", columnList = "college_id"),
                @Index(name = "idx_library_book_isbn", columnList = "isbn"),
                @Index(name = "idx_library_book_category", columnList = "category")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LibraryBook extends BaseEntity {

    /**
     * Tenant (School / College)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * ISBN (can be null for internal books)
     */
    @Column(length = 20)
    private String isbn;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 150)
    private String author;


    @Column(length = 150)
    private String publisher;

    /**
     * Category / Genre (Science, Fiction, Reference, etc.)
     */
    @Column(length = 100)
    private String category;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private Set<LibraryIssue> issues;

    /**
     * Total copies owned
     */
    @Column(name = "total_copies", nullable = false)
    private Integer totalCopies;

    /**
     * Available copies (derived but stored for performance)
     */
    @Column(name = "available_copies", nullable = false)
    private Integer availableCopies;

}



