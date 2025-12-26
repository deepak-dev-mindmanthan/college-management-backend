package org.collegemanagement.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {

    private String uuid;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String category;
    private Integer totalCopies;
    private Integer availableCopies;
    private Long collegeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

