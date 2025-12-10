package org.collegemanagement.dto;



import lombok.Builder;
import lombok.Data;
import org.collegemanagement.entity.College;
import org.collegemanagement.entity.Role;
import org.collegemanagement.entity.User;

import java.util.HashSet;
import java.util.Set;

@Builder
@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private Set<Role> roles;
    private College college;


    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .college(user.getCollege())
                .roles(new HashSet<>(user.getRoles()))
                .build();
    }

    public static User toEntity(UserDto dto) {
        return User.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .college(dto.getCollege())
                .roles(dto.getRoles())
                .build();
    }
}

