package org.collegemanagement.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibrarySummary {

    private Long totalBooks;
    private Long totalCopies;
    private Long availableCopies;
    private Long issuedBooks;
    private Long overdueBooks;
    private BigDecimal totalFines;
    private BigDecimal pendingFines;
}

