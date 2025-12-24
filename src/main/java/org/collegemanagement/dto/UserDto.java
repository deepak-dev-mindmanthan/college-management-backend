package org.collegemanagement.dto;



import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

import org.collegemanagement.enums.Status;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private String uuid;

    private String name;
    private String email;

    private Status status;
    private Boolean emailVerified;

    private Instant lastLoginAt;

    private Set<String> roles;        // ROLE_ADMIN, ROLE_TEACHER
    private Long collegeId;
    private String collegeName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ---------- Mapping ----------

    public static UserDto fromEntity(org.collegemanagement.entity.user.User user) {
        return UserDto.builder()
                .uuid(user.getUuid())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .roles(
                        user.getRoles() == null
                                ? Set.of()
                                : user.getRoles()
                                .stream()
                                .map(role -> role.getName().name())
                                .collect(java.util.stream.Collectors.toSet())
                )
                .collegeId(user.getCollege() != null ? user.getCollege().getId() : null)
                .collegeName(user.getCollege() != null ? user.getCollege().getName() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
