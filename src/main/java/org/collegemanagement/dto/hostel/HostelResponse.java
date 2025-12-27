package org.collegemanagement.dto.hostel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.HostelType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostelResponse {

    private String uuid;
    private String name;
    private HostelType type;
    private Integer capacity;
    private String wardenUuid;
    private String wardenName;
    private Long collegeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

