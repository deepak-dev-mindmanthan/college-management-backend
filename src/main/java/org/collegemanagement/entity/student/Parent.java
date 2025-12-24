package org.collegemanagement.entity.student;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "parents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_parent_user",
                        columnNames = {"user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_parent_college", columnList = "college_id"),
                @Index(name = "idx_parent_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Parent extends BaseEntity {

    /**
     * Tenant (College)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Login account
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<ParentStudent> children = new HashSet<>();


    @Column(length = 150)
    private String occupation;
}
