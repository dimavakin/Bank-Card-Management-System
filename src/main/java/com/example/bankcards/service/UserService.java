package com.example.bankcards.service;

import com.example.bankcards.dto.JwtAuthenticationDto;
import com.example.bankcards.dto.RefreshTokenDto;
import com.example.bankcards.dto.UserCredentialsDto;
import org.springframework.security.authentication.BadCredentialsException;

import javax.naming.AuthenticationException;

public interface UserService {
    JwtAuthenticationDto singIn(UserCredentialsDto userCredentialsDto) throws BadCredentialsException;

    JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws Exception;

}
