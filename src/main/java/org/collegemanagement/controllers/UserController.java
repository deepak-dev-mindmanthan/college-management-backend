package org.collegemanagement.controllers;

import org.collegemanagement.entity.user.User;
import org.collegemanagement.dto.UserDto;
import org.collegemanagement.services.UserManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserManager userManager;

    public UserController(UserManager userManager) {
        this.userManager = userManager;
    }

    @GetMapping("/{id}")
    @PreAuthorize("#user.id == #id")
    public ResponseEntity<UserDto> user(@AuthenticationPrincipal User user, @PathVariable Long id) {
        System.out.println(user.getId());
        return ResponseEntity.ok(userManager.getUserById(id));
    }
}