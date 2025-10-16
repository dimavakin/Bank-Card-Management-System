package com.example.bankcards.service.impl;

import com.example.bankcards.dto.NewUserDto;
import com.example.bankcards.dto.UpdateUserDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.DuplicatedDataException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminServiceImpl adminService;

    private NewUserDto newUserDto;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        newUserDto = new NewUserDto();
        newUserDto.setFirstName("John");
        newUserDto.setLastName("Doe");
        newUserDto.setEmail("john@example.com");
        newUserDto.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        user.setPassword("{bcrypt}encodedPassword123");
        user.setRoles(Set.of(Role.ROLE_USER));

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setEmail("john@example.com");
    }

    @Test
    void addUser_Success_ReturnsUserDto() {
        when(passwordEncoder.encode(newUserDto.getPassword())).thenReturn("{bcrypt}encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = adminService.addUser(newUserDto);

        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getFirstName(), result.getFirstName());
        assertEquals(userDto.getLastName(), result.getLastName());
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addUser_DuplicateEmail_ThrowsException() {
        when(passwordEncoder.encode(newUserDto.getPassword())).thenReturn("{bcrypt}encodedPassword123");
        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("Duplicate email"));

        assertThrows(RuntimeException.class, () -> adminService.addUser(newUserDto));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        Long userId = 1L;
        adminService.deleteUser(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_NonExistentUser_Ignored() {
        Long userId = 999L;
        adminService.deleteUser(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void updateUser_UpdateAllFields_Success() {
        Long userId = 1L;
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setFirstName("Jane");
        updateDto.setLastName("Smith");
        updateDto.setEmail("jane@example.com");
        updateDto.setRoles(Set.of(Role.ROLE_ADMIN));

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");
        existingUser.setEmail("john@example.com");
        existingUser.setRoles(Set.of(Role.ROLE_USER));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = adminService.updateUser(userId, updateDto);

        assertEquals("Jane", existingUser.getFirstName());
        assertEquals("Smith", existingUser.getLastName());
        assertEquals("jane@example.com", existingUser.getEmail());
        assertTrue(existingUser.getRoles().contains(Role.ROLE_ADMIN));
        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail("jane@example.com");
        verify(userRepository).save(existingUser);
        assertEquals("Jane", result.getFirstName());
    }

    @Test
    void updateUser_PartialUpdate_OnlyFirstName() {
        Long userId = 1L;
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setFirstName("Jane");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");
        existingUser.setEmail("john@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = adminService.updateUser(userId, updateDto);

        assertEquals("Jane", existingUser.getFirstName());
        assertEquals("Doe", existingUser.getLastName());
        assertEquals("john@example.com", existingUser.getEmail());
        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void updateUser_EmailAlreadyExists_ThrowsValidationException() {
        Long userId = 1L;
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setEmail("existing@example.com");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("john@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        DuplicatedDataException exception = assertThrows(DuplicatedDataException.class,
                () -> adminService.updateUser(userId, updateDto));
        assertEquals("Email already exists: existing@example.com", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_SameEmail_NoCheckNeeded() {
        Long userId = 1L;
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setEmail("john@example.com");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("john@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = adminService.updateUser(userId, updateDto);

        assertEquals("john@example.com", existingUser.getEmail());
        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
        verify(userRepository, never()).existsByEmail(any());
    }
}