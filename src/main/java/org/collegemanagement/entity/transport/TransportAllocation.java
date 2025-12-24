package org.collegemanagement.entity.transport;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.student.Student;

import java.time.Instant;

@Entity
@Table(
        name = "transport_allocations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_active_transport_per_student",
                        columnNames = {"student_id", "route_id"}
                )
        },
        indexes = {
                @Index(name = "idx_transport_student", columnList = "student_id"),
                @Index(name = "idx_transport_route", columnList = "route_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TransportAllocation extends BaseEntity {

    /**
     * Student using transport
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Assigned transport route
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private TransportRoute route;

    /**
     * Pickup point / stop name
     */
    @Column(name = "pickup_point", nullable = false, length = 200)
    private String pickupPoint;

    /**
     * Allocation start time
     */
    @Column(name = "allocated_at", nullable = false)
    private Instant allocatedAt;

    /**
     * Release time (null = active)
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

