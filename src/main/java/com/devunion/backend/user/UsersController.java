package com.devunion.backend.user;

import com.devunion.backend.user.dto.UserProfileResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    //JWT 인증된 사용자 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getMyProfile(Authentication authentication) {
        String email = authentication.getName(); //JWT에서 세팅된 이메일
        UserProfileResponseDto profile = userService.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }
}
