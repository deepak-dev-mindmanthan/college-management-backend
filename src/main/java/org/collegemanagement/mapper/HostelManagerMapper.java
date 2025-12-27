package org.collegemanagement.mapper;

import org.collegemanagement.dto.hostel.HostelManagerResponse;
import org.collegemanagement.entity.staff.StaffProfile;
import org.collegemanagement.entity.user.User;

public final class HostelManagerMapper {

    private HostelManagerMapper() {
    }

    /**
     * Convert User entity with StaffProfile to HostelManagerResponse
     */
    public static HostelManagerResponse toResponse(User user, StaffProfile staffProfile) {
        if (user == null) {
            return null;
        }

        HostelManagerResponse.HostelManagerResponseBuilder builder = HostelManagerResponse.builder()
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
     * Convert User entity to HostelManagerResponse (without StaffProfile)
     */
    public static HostelManagerResponse toResponse(User user) {
        return toResponse(user, null);
    }
}

