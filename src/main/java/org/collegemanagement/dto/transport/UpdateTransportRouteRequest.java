package org.collegemanagement.dto.transport;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransportRouteRequest {

    @Size(min = 2, max = 150, message = "Route name must be between 2 and 150 characters")
    private String routeName;

    @Size(max = 50, message = "Vehicle number must not exceed 50 characters")
    private String vehicleNo;

    @Size(max = 150, message = "Driver name must not exceed 150 characters")
    private String driverName;
}

