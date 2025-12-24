package org.collegemanagement.entity.hostel;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.HostelType;

@Entity
@Table(
        name = "hostels",
        indexes = {
                @Index(name = "idx_hostel_college", columnList = "college_id"),
                @Index(name = "idx_hostel_type", columnList = "type"),
                @Index(name = "idx_hostel_warden", columnList = "warden_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Hostel extends BaseEntity {

    /**
     * Tenant (College)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(nullable = false, length = 150)
    private String name;

    /**
     * BOYS / GIRLS
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private HostelType type;

    /**
     * Total capacity of hostel
     */
    @Column(nullable = false)
    private Integer capacity;

    /**
     * Hostel warden (Staff / Admin user)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warden_id")
    private User warden;
}

