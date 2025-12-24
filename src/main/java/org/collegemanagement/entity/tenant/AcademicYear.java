package org.collegemanagement.entity.tenant;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.base.BaseEntity;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(
        name = "academic_years",
        indexes = {
                @Index(name = "idx_academic_year_tenant", columnList = "college_id"),
                @Index(name = "idx_academic_year_active", columnList = "college_id, is_active"),
                @Index(name = "idx_academic_year_dates", columnList = "start_date, end_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AcademicYear extends BaseEntity {

    /**
     * Tenant (FK)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Display name e.g. "2024–25"
     */
    @Column(name = "year_name", nullable = false, length = 50)
    private String yearName;


    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "academicYear", fetch = FetchType.LAZY)
    private Set<ClassRoom> classes;

    /**
     * Only one active year per tenant
     */
    @Column(name = "is_active", nullable = false)
    private Boolean active = false;

    /**
     * Defaults & validation hook
     */
    @PrePersist
    protected void onCreate() {
        if (this.active == null) {
            this.active = false;
        }
    }
}
