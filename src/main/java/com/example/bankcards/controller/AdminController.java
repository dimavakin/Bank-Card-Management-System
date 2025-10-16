package com.example.bankcards.controller;

import com.example.bankcards.dto.NewUserDto;
import com.example.bankcards.dto.UpdateUserDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    AdminService adminService;

    @PostMapping("/user")
    @Operation(summary = "Create new user")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody NewUserDto newUserDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.addUser(newUserDto));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserDto updateUserDto) {

        UserDto updatedUser = adminService.updateUser(userId, updateUserDto);
        return ResponseEntity.ok(updatedUser);
    }
}
