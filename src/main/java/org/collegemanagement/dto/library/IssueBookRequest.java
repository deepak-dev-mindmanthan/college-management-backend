package org.collegemanagement.dto.library;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueBookRequest {

    @NotBlank(message = "Book UUID is required")
    private String bookUuid;

    @NotBlank(message = "User UUID is required")
    private String userUuid;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
}

