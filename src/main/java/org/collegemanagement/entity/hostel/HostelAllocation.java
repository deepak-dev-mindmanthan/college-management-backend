package org.collegemanagement.entity.hostel;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.student.Student;

import java.time.Instant;

@Entity
@Table(
        name = "hostel_allocations",
        indexes = {
                @Index(name = "idx_allocation_student", columnList = "student_id"),
                @Index(name = "idx_allocation_room", columnList = "room_id"),
                @Index(name = "idx_allocation_active", columnList = "released_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HostelAllocation extends BaseEntity {

    /**
     * Student allocated to hostel room
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Room assigned
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private HostelRoom room;

    /**
     * Allocation start time
     */
    @Column(name = "allocated_at", nullable = false)
    private Instant allocatedAt;

    /**
     * Release time (null = currently staying)
     */
    @Column(name = "released_at")
    private Instant releasedAt;

    @PrePersist
    protected void onCreate() {
        if (allocatedAt == null) {
            allocatedAt = Instant.now();
        }
    }
}

