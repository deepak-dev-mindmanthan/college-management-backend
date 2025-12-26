package org.collegemanagement.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.RelationType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentInfo {
    private String uuid;
    private String name;
    private String email;
    private String occupation;
    private RelationType relation;
}

