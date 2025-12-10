package org.collegemanagement.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookRequest {
    private Long id;
    private Long collegeId;
    private String title;
    private String author;
    private String isbn;
    private Integer totalCopies;
}

