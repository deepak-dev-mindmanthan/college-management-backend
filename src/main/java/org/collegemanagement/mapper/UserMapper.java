package org.collegemanagement.mapper;

import org.collegemanagement.dto.UserSummary;
import org.collegemanagement.entity.user.User;

import java.util.stream.Collectors;

import org.collegemanagement.dto.UserDto;
import org.collegemanagement.entity.user.Role;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserSummary toSummary(User user) {

        if (user == null) {
            return null;
        }

        return UserSummary.builder()
                .uuid(user.getUuid())
                .email(user.getEmail())
                .roles(
                        user.getRoles() == null
                                ? Collections.emptySet()
                                : user.getRoles()
                                .stream()
                                .filter(Objects::nonNull)
                                .map(r -> r.getName().name())
                                .collect(Collectors.toSet())
                )
                .collegeId(
                        user.getCollege() != null
                                ? user.getCollege().getId()
                                : null
                )
                .build();
    }

    /**
     * ENTITY → DTO
     */
    public static UserDto toDto(User user) {

        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .uuid(user.getUuid())
                .uuid(user.getUuid())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .roles(
                        user.getRoles() == null
                                ? Collections.emptySet()
                                : user.getRoles()
                                .stream()
                                .map(r -> r.getName().name())
                                .collect(Collectors.toSet())
                )
                .collegeId(
                        user.getCollege() != null
                                ? user.getCollege().getId()
                                : null
                )
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * DTO → ENTITY (SAFE)
     */
    public static User toEntity(UserDto dto, User target) {
        /*
         * IMPORTANT:
         * - target is an existing-managed entity (for UPDATE)
         * - we do NOT touch password, roles, defaults here
         */

        if (dto == null || target == null) {
            return target;
        }

        if (dto.getName() != null) {
            target.setName(dto.getName());
        }

        if (dto.getEmail() != null) {
            target.setEmail(dto.getEmail());
        }

        // status update allowed only if explicitly provided
        if (dto.getStatus() != null) {
            target.setStatus(dto.getStatus());
        }

        // email verification flag
        if (dto.getEmailVerified() != null) {
            target.setEmailVerified(dto.getEmailVerified());
        }

        return target;
    }

    /**
     * ROLE STRING → ENTITY
     * (Optional helper)
     */
    public static Set<Role> mapRoles(Set<Role> existingRoles) {
        return existingRoles == null ? Collections.emptySet() : existingRoles;
    }
}
