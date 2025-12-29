package org.collegemanagement.entity.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.academic.ClassSubjectTeacher;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.communication.Notification;
import org.collegemanagement.entity.hostel.Hostel;
import org.collegemanagement.entity.leave.LeaveRequest;
import org.collegemanagement.entity.library.LibraryIssue;
import org.collegemanagement.entity.staff.StaffProfile;
import org.collegemanagement.entity.student.Parent;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.tenant.Department;
import org.collegemanagement.entity.timetable.Timetable;
import org.collegemanagement.enums.Status;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

public class User extends BaseEntity implements UserDetails {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;  // Hashed password

    @Column(name = "is_email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(nullable = false)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "classTeacher", fetch = FetchType.LAZY)
    private Set<ClassRoom> assignedClasses;

    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private Set<ClassSubjectTeacher> teachingAssignments;


    @ManyToOne
    @JoinColumn(name = "college_id", nullable = true) // Super Admin doesn't belong to a college
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private College college;

    @OneToMany(mappedBy = "head", fetch = FetchType.LAZY)
    private Set<Department> headedDepartments = new HashSet<>();

    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private Set<Timetable> teachingTimetable;


    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
    private Student student;

    @OneToMany(mappedBy = "issuedTo", fetch = FetchType.LAZY)
    private Set<LibraryIssue> borrowedBooks;


    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
    private Parent parent;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<LeaveRequest> leaveRequests;

    @OneToMany(mappedBy = "approvedBy", fetch = FetchType.LAZY)
    private Set<LeaveRequest> approvedLeaves;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private StaffProfile staffProfile;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Notification> notifications;

    @OneToMany(mappedBy = "warden", fetch = FetchType.LAZY)
    private Set<Hostel> managedHostels;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = Status.ACTIVE;
        }

        if (this.emailVerified == null) {
            this.emailVerified = false;
        }
    }

    public User(Long id, String email, String password) {
        super();
        this.setId(id);
        this.email = email;
        this.password = password;
    }

    public User(String name, String email, String password) {
        super();
        this.email = email;
        this.password = password;
        this.name = name;
    }

    // Methods required by Spring Security for user details
    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles == null
                ? Collections.emptySet()
                : roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toUnmodifiableSet());
    }


    @NonNull
    @Override
    public String getUsername() {
        return email;
    }
}