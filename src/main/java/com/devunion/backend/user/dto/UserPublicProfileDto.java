package com.devunion.backend.user.dto;

import com.devunion.backend.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPublicProfileDto {
    private Long id;
    private String username;
    private String major;
    private Integer grade;

    public static UserPublicProfileDto from(User u) {
        return new UserPublicProfileDto(u.getId(), u.getUsername(), u.getMajor(), u.getGrade());
    }

}
