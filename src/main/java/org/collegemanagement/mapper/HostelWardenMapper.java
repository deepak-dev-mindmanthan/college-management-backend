package org.collegemanagement.mapper;

import org.collegemanagement.dto.hostel.HostelWardenResponse;
import org.collegemanagement.entity.staff.StaffProfile;
import org.collegemanagement.entity.user.User;

import java.util.Collections;
import java.util.List;

public final class HostelWardenMapper {

    private HostelWardenMapper() {
    }

    /**
     * Convert User entity with StaffProfile to HostelWardenResponse
     */
    public static HostelWardenResponse toResponse(User user, StaffProfile staffProfile, List<String> assignedHostelUuids) {
        if (user == null) {
            return null;
        }

        HostelWardenResponse.HostelWardenResponseBuilder builder = HostelWardenResponse.builder()
                .uuid(user.getUuid())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .collegeId(user.getCollege() != null ? user.getCollege().getId() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .assignedHostelUuids(assignedHostelUuids != null ? assignedHostelUuids : Collections.emptyList())
                .assignedHostelCount(assignedHostelUuids != null ? assignedHostelUuids.size() : 0);

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
     * Convert User entity to HostelWardenResponse (without StaffProfile and assigned hostels)
     */
    public static HostelWardenResponse toResponse(User user) {
        return toResponse(user, null, null);
    }

    /**
     * Convert User entity with StaffProfile to HostelWardenResponse (without assigned hostels)
     */
    public static HostelWardenResponse toResponse(User user, StaffProfile staffProfile) {
        return toResponse(user, staffProfile, null);
    }
}

