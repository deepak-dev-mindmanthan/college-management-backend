package org.collegemanagement.dto;


import lombok.*;
import org.collegemanagement.enums.Status;
import org.collegemanagement.entity.College;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollegeDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Status status;
    private List<UserDto> users;
    private SubscriptionDto subscription;

    // Convert from Entity to DTO
    public static CollegeDto fromEntity(College college) {
        return CollegeDto.builder()
                .id(college.getId())
                .name(college.getName())
                .email(college.getEmail())
                .phone(college.getPhone())
                .address(college.getAddress())
                .status(college.getStatus())
                .subscription(SubscriptionDto.fromEntity(college.getSubscription()))
                .users(college.getUsers() != null
                        ? college.getUsers().stream().map(UserDto::fromEntity).collect(Collectors.toList())
                        : null)
                .build();
    }

    // Convert from DTO to Entity
    public static College toEntity(CollegeDto dto) {
        return College.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .status(dto.getStatus())
                // Subscription is managed separately to avoid accidental duplicates
                .subscription(null)
                .users(dto.getUsers() != null
                        ? dto.getUsers().stream().map(UserDto::toEntity).collect(Collectors.toList())
                        : null)
                .build();
    }
}
