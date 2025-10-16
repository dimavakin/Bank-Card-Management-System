package com.bankcards.service;

import com.bankcards.dto.JwtAuthenticationDto;
import com.bankcards.dto.RefreshTokenDto;
import com.bankcards.dto.UserCredentialsDto;
import org.springframework.security.authentication.BadCredentialsException;

public interface UserService {
    JwtAuthenticationDto singIn(UserCredentialsDto userCredentialsDto) throws BadCredentialsException;

    JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws Exception;

}
