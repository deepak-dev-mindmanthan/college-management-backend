package org.collegemanagement.entity.ptm;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.user.User;
import org.hibernate.annotations.Parent;

import java.time.Instant;

@Entity
@Table(
        name = "ptm_bookings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_slot_single_booking",
                        columnNames = {"slot_id"}
                )
        },
        indexes = {
                @Index(name = "idx_ptm_booking_slot", columnList = "slot_id"),
                @Index(name = "idx_ptm_booking_parent", columnList = "parent_id"),
                @Index(name = "idx_ptm_booking_student", columnList = "student_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PTMBooking extends BaseEntity {

    /**
     * Booked PTM slot
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private PTMSlot slot;

    /**
     * Parent who booked the slot
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    /**
     * Student concerned in the meeting
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Booking timestamp
     */

    @Column(name = "booked_at", nullable = false, updatable = false)
    private Instant bookedAt = Instant.now();

    /**
     * Optional remarks by parent
     */
    @Column(length = 500)
    private String remarks;



}
