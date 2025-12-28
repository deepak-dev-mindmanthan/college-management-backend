package org.collegemanagement.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.ResultStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishTranscriptRequest {

    private ResultStatus resultStatus;
    private String remarks;
}

