package org.collegemanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)

public class LoginResponse {
    private UserSummary user;
    private SubscriptionSummary subscription;
    private Token auth;
}
