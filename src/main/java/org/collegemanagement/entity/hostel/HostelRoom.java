package org.collegemanagement.entity.hostel;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;

@Entity
@Table(
        name = "hostel_rooms",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_room_per_hostel",
                        columnNames = {"hostel_id", "room_number"}
                )
        },
        indexes = {
                @Index(name = "idx_room_hostel", columnList = "hostel_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HostelRoom extends BaseEntity {

    /**
     * Hostel to which room belongs
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    /**
     * Max occupants
     */
    @Column(nullable = false)
    private Integer capacity;
}

