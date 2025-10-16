package com.example.bankcards.service.impl;

import com.example.bankcards.dto.JwtAuthenticationDto;
import com.example.bankcards.dto.RefreshTokenDto;
import com.example.bankcards.dto.UserCredentialsDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.security.CustomUserDetailsServiceImpl;
import com.example.bankcards.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomUserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserCredentialsDto credentialsDto;
    private RefreshTokenDto refreshTokenDto;
    private JwtAuthenticationDto jwtDto;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        user.setPassword("{bcrypt}encodedPassword123");
        user.setRoles(Set.of(Role.ROLE_USER));
        credentialsDto = new UserCredentialsDto();
        credentialsDto.setEmail("john@example.com");
        credentialsDto.setPassword("password123");

        refreshTokenDto = new RefreshTokenDto();
        refreshTokenDto.setRefreshToken("valid-refresh-token");

        jwtDto = new JwtAuthenticationDto();
        jwtDto.setToken("access-token");
        jwtDto.setRefreshToken("refresh-token");

        userDetails = new CustomUserDetails(user);
    }


    @Test
    void singIn_ValidCredentials_ReturnsJwtDto() throws BadCredentialsException {
        when(userRepository.findByEmail(credentialsDto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(credentialsDto.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateAuthToken(eq(user.getEmail()), anyCollection())).thenReturn(jwtDto);

        JwtAuthenticationDto result = userService.singIn(credentialsDto);

        assertEquals(jwtDto, result);
        verify(userRepository).findByEmail(credentialsDto.getEmail());
        verify(passwordEncoder).matches("password123", user.getPassword());
        verify(jwtService).generateAuthToken(eq("john@example.com"), anyCollection());
    }

    @Test
    void singIn_UserNotFound_ThrowsAuthenticationException() {
        when(userRepository.findByEmail(credentialsDto.getEmail())).thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> userService.singIn(credentialsDto));
        assertEquals("Email or Password is not correct", exception.getMessage());
        verify(userRepository).findByEmail(credentialsDto.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateAuthToken(any(), any());
    }

    @Test
    void singIn_WrongPassword_ThrowsAuthenticationException() {
        when(userRepository.findByEmail(credentialsDto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(credentialsDto.getPassword(), user.getPassword())).thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> userService.singIn(credentialsDto));
        assertEquals("Email or Password is not correct", exception.getMessage());
        verify(userRepository).findByEmail(credentialsDto.getEmail());
        verify(passwordEncoder).matches("password123", user.getPassword());
        verify(jwtService, never()).generateAuthToken(any(), any());
    }

    @Test
    void refreshToken_ValidToken_ReturnsNewJwtDto() throws Exception {
        when(jwtService.validateJwtToken(refreshTokenDto.getRefreshToken())).thenReturn(true);
        when(jwtService.getEmailFromToken(refreshTokenDto.getRefreshToken())).thenReturn("john@example.com");
        when(userDetailsService.loadUserByUsername("john@example.com")).thenReturn(userDetails);
        when(jwtService.refreshBaseToken(eq("john@example.com"), eq("valid-refresh-token"), anyCollection()))
                .thenReturn(jwtDto);

        JwtAuthenticationDto result = userService.refreshToken(refreshTokenDto);

        assertEquals(jwtDto, result);
        verify(jwtService).validateJwtToken("valid-refresh-token");
        verify(jwtService).getEmailFromToken("valid-refresh-token");
        verify(userDetailsService).loadUserByUsername("john@example.com");
        verify(jwtService).refreshBaseToken(eq("john@example.com"), eq("valid-refresh-token"), anyCollection());
    }

    @Test
    void refreshToken_NullToken_ThrowsException() {
        refreshTokenDto.setRefreshToken(null);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userService.refreshToken(refreshTokenDto));
        assertEquals("Invalid refresh token", exception.getMessage());
        verify(jwtService, never()).validateJwtToken(any());
    }

    @Test
    void refreshToken_InvalidToken_ThrowsException() {
        when(jwtService.validateJwtToken(refreshTokenDto.getRefreshToken())).thenReturn(false);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userService.refreshToken(refreshTokenDto));
        assertEquals("Invalid refresh token", exception.getMessage());
        verify(jwtService).validateJwtToken("valid-refresh-token");
        verify(jwtService, never()).getEmailFromToken(any());
    }

    @Test
    void findByCredentials_Valid_CalledFromSignIn() throws BadCredentialsException {
        when(userRepository.findByEmail(credentialsDto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(credentialsDto.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateAuthToken(eq(user.getEmail()), anyCollection())).thenReturn(jwtDto);

        userService.singIn(credentialsDto);

        verify(userRepository).findByEmail(credentialsDto.getEmail());
        verify(passwordEncoder).matches("password123", user.getPassword());
    }

    @Test
    void findByCredentials_InvalidEmail_CalledFromSignIn() {
        when(userRepository.findByEmail(credentialsDto.getEmail())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> userService.singIn(credentialsDto));
        verify(userRepository).findByEmail(credentialsDto.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void findByCredentials_InvalidPassword_CalledFromSignIn() {
        when(userRepository.findByEmail(credentialsDto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(credentialsDto.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.singIn(credentialsDto));
        verify(passwordEncoder).matches("password123", user.getPassword());
    }

    @Test
    void refreshToken_ValidButUserNotFound_ThrowsException() {
        when(jwtService.validateJwtToken(refreshTokenDto.getRefreshToken())).thenReturn(true);
        when(jwtService.getEmailFromToken(refreshTokenDto.getRefreshToken())).thenReturn("unknown@example.com");
        when(userDetailsService.loadUserByUsername("unknown@example.com"))
                .thenThrow(new NotFoundException("User not found"));

        assertThrows(Exception.class, () -> userService.refreshToken(refreshTokenDto));
        verify(userDetailsService).loadUserByUsername("unknown@example.com");
    }

    @Test
    void singIn_NullCredentials_ThrowsException() {
        assertThrows(NullPointerException.class, () -> userService.singIn(null));
    }

    @Test
    void refreshToken_EmptyToken_ThrowsException() {
        refreshTokenDto.setRefreshToken("");
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userService.refreshToken(refreshTokenDto));
        assertEquals("Invalid refresh token", exception.getMessage());
    }

}