package org.collegemanagement.dto.hostel;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHostelRoomRequest {

    @Size(max = 20, message = "Room number must not exceed 20 characters")
    private String roomNumber;

    @Positive(message = "Capacity must be positive")
    private Integer capacity;
}

