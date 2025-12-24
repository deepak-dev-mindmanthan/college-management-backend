package org.collegemanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSummary {
    private String uuid;
    private String email;
    private Set<String> roles;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long collegeId;
}

