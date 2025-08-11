package com.devunion.backend.user;

import com.devunion.backend.user.dto.UserProfileResponseDto;
import com.devunion.backend.user.dto.UserPublicProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("/{id}")
    public ResponseEntity<UserPublicProfileDto> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getPublicProfileById(id));
    }

    @GetMapping("/lookup")
    public ResponseEntity<UserPublicProfileDto> findByUsername(@RequestParam String username) {
        return userService.getPublicProfileByUsernameOpt(username)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다")
                );

    }
}
