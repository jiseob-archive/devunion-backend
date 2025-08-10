package com.devunion.backend.user.dto;

import com.devunion.backend.user.Role;
import com.devunion.backend.user.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponseDto {
    private Long id;
    private String email;
    private String username;
    private String major;
    private Integer grade;
    private Role role;

    public static UserProfileResponseDto from(User user) {
        return UserProfileResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .major(user.getMajor())
                .grade(user.getGrade())
                .role(user.getRole())
                .build();
    }
}