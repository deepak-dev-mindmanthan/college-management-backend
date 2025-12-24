package org.collegemanagement.entity.transport;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.tenant.College;

@Entity
@Table(
        name = "transport_routes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_route_per_college",
                        columnNames = {"college_id", "route_name"}
                )
        },
        indexes = {
                @Index(name = "idx_transport_college", columnList = "college_id"),
                @Index(name = "idx_transport_vehicle", columnList = "vehicle_no")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TransportRoute extends BaseEntity {

    /**
     * Tenant (School / College)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Route name
     * Example: Route A – City Center
     */
    @Column(name = "route_name", nullable = false, length = 150)
    private String routeName;

    /**
     * Vehicle number
     */
    @Column(name = "vehicle_no", nullable = false, length = 50)
    private String vehicleNo;

    /**
     * Driver name (can later be replaced with Driver entity)
     */
    @Column(name = "driver_name", length = 150)
    private String driverName;
}

