package org.collegemanagement.entity.ptm;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "ptm_slots",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_teacher_slot_time",
                        columnNames = {"teacher_id", "date", "start_time", "end_time"}
                )
        },
        indexes = {
                @Index(name = "idx_ptm_slot_college", columnList = "college_id"),
                @Index(name = "idx_ptm_slot_teacher", columnList = "teacher_id"),
                @Index(name = "idx_ptm_slot_date", columnList = "date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PTMSlot extends BaseEntity {

    /**
     * Tenant (School / College)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Teacher offering the slot
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User parentUser;


    /**
     * Date of PTM
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Slot start time
     */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /**
     * Slot end time
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Whether slot is active / cancelled
     */
    @Column(nullable = false)
    private Boolean active = true;
}

