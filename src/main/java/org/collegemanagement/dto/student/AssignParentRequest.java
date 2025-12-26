package org.collegemanagement.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.RelationType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignParentRequest {

    @NotBlank(message = "Parent UUID is required")
    private String parentUuid;

    @NotNull(message = "Relation type is required")
    private RelationType relation;
}

