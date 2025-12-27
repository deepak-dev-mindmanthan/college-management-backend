package org.collegemanagement.mapper;

import org.collegemanagement.dto.librarian.LibrarianResponse;
import org.collegemanagement.entity.staff.StaffProfile;
import org.collegemanagement.entity.user.User;

public final class LibrarianMapper {

    private LibrarianMapper() {
    }

    /**
     * Convert User entity with StaffProfile to LibrarianResponse
     */
    public static LibrarianResponse toResponse(User user, StaffProfile staffProfile) {
        if (user == null) {
            return null;
        }

        LibrarianResponse.LibrarianResponseBuilder builder = LibrarianResponse.builder()
                .uuid(user.getUuid())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .collegeId(user.getCollege() != null ? user.getCollege().getId() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        if (staffProfile != null) {
            builder.designation(staffProfile.getDesignation())
                    .salary(staffProfile.getSalary())
                    .phone(staffProfile.getPhone())
                    .address(staffProfile.getAddress())
                    .joiningDate(staffProfile.getJoiningDate());
        }

        return builder.build();
    }

    /**
     * Convert User entity to LibrarianResponse (without StaffProfile)
     */
    public static LibrarianResponse toResponse(User user) {
        return toResponse(user, null);
    }
}

