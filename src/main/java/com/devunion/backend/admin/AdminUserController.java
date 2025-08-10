package com.devunion.backend.admin;

import com.devunion.backend.user.Role;
import com.devunion.backend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @PatchMapping("/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')") // 운영자만 접근 가능
    public ResponseEntity<Void> promoteToAdmin(@PathVariable Long id) {
        userService.updateRole(id, Role.ADMIN);
        return ResponseEntity.noContent().build();
    }


}
