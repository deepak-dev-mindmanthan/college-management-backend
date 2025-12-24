package org.collegemanagement.entity.tenant;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.academic.Subject;
import org.collegemanagement.entity.subscription.Subscription;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.enums.Status;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "colleges",
        indexes = {
                @Index(name = "idx_college_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class College extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "short_code", nullable = false, unique = true, length = 20)
    private String shortCode;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String city;

    @OneToMany(mappedBy = "college", fetch = FetchType.LAZY)
    private Set<Department> departments = new HashSet<>();
    
    private String address;

    @OneToMany(
            mappedBy = "college",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<AcademicYear> academicYears = new HashSet<>();

    @OneToMany(mappedBy = "college", fetch = FetchType.LAZY)
    private Set<ClassRoom> classes;

//    @OneToMany(mappedBy = "college", fetch = FetchType.LAZY)
//    private Set<Subject> subjects = new HashSet<>();
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE; // ACTIVE, SUSPENDED

    @OneToOne(mappedBy = "college", cascade = CascadeType.ALL, fetch = FetchType.EAGER,orphanRemoval = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Subscription subscription;

    @OneToMany(mappedBy = "college", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<User> users;
}
