package org.collegemanagement.mapper;

import org.collegemanagement.dto.teacher.ClassSubjectInfo;
import org.collegemanagement.dto.teacher.TeacherDetailResponse;
import org.collegemanagement.dto.teacher.TeacherResponse;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.academic.ClassSubjectTeacher;
import org.collegemanagement.entity.academic.Subject;
import org.collegemanagement.entity.staff.StaffProfile;
import org.collegemanagement.entity.tenant.Department;
import org.collegemanagement.entity.user.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class TeacherMapper {

    private TeacherMapper() {
    }

    /**
     * Convert User entity with StaffProfile to TeacherResponse
     */
    public static TeacherResponse toResponse(User user, StaffProfile staffProfile) {
        if (user == null) {
            return null;
        }

        TeacherResponse.TeacherResponseBuilder builder = TeacherResponse.builder()
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
     * Convert User entity to TeacherResponse (without StaffProfile)
     */
    public static TeacherResponse toResponse(User user) {
        return toResponse(user, null);
    }

    /**
     * Convert User entity with StaffProfile to TeacherDetailResponse
     */
    public static TeacherDetailResponse toDetailResponse(User user, StaffProfile staffProfile) {
        if (user == null) {
            return null;
        }

        TeacherDetailResponse.TeacherDetailResponseBuilder builder = TeacherDetailResponse.builder()
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
                    .joiningDate(staffProfile.getJoiningDate());
        }

        // Map assigned classes and subjects
        List<ClassSubjectInfo> assignedClasses = Collections.emptyList();
        if (user.getTeachingAssignments() != null && !user.getTeachingAssignments().isEmpty()) {
            assignedClasses = user.getTeachingAssignments().stream()
                    .map(TeacherMapper::mapClassSubjectInfo)
                    .collect(Collectors.toList());
        }

        builder.assignedClasses(assignedClasses);

        // Map headed departments
        List<String> headedDepartments = Collections.emptyList();
        if (user.getHeadedDepartments() != null && !user.getHeadedDepartments().isEmpty()) {
            headedDepartments = user.getHeadedDepartments().stream()
                    .map(Department::getName)
                    .collect(Collectors.toList());
        }
        builder.headedDepartments(headedDepartments);

        // Calculate totals
        long totalSubjects = user.getTeachingAssignments() != null
                ? user.getTeachingAssignments().stream()
                .map(ClassSubjectTeacher::getSubject)
                .distinct()
                .count()
                : 0;
        builder.totalSubjects(totalSubjects);

        // Total students would require additional query, set to null for now
        builder.totalStudents(null);

        return builder.build();
    }

    private static ClassSubjectInfo mapClassSubjectInfo(ClassSubjectTeacher cst) {
        ClassRoom classRoom = cst.getClassRoom();
        Subject subject = cst.getSubject();

        return ClassSubjectInfo.builder()
                .classUuid(classRoom != null ? classRoom.getUuid() : null)
                .className(classRoom != null ? classRoom.getName() : null)
                .section(classRoom != null ? classRoom.getSection() : null)
                .subjectUuid(subject != null ? subject.getUuid() : null)
                .subjectName(subject != null ? subject.getName() : null)
                .subjectCode(subject != null ? subject.getCode() : null)
                .build();
    }
}

