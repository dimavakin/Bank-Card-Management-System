package com.bankcards.service.impl;

import com.bankcards.dto.JwtAuthenticationDto;
import com.bankcards.dto.RefreshTokenDto;
import com.bankcards.dto.UserCredentialsDto;
import com.bankcards.entity.User;
import com.bankcards.repository.UserRepository;
import com.bankcards.security.CustomUserDetails;
import com.bankcards.security.CustomUserDetailsServiceImpl;
import com.bankcards.security.jwt.JwtService;
import com.bankcards.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    JwtService jwtService;
    PasswordEncoder passwordEncoder;
    CustomUserDetailsServiceImpl userDetailsService;

    @Override
    public JwtAuthenticationDto singIn(UserCredentialsDto userCredentialsDto) throws BadCredentialsException {
        User user = findByCredentials(userCredentialsDto);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        return jwtService.generateAuthToken(user.getEmail(), authorities);
    }

    @Override
    public JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto){
        String refreshToken = refreshTokenDto.getRefreshToken();
        if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {
            String email = jwtService.getEmailFromToken(refreshToken);
            CustomUserDetails userDetails = userDetailsService.loadUserByUsername(email);
            return jwtService.refreshBaseToken(email, refreshToken, userDetails.getAuthorities());
        }
        throw new AccessDeniedException("Invalid refresh token");
    }

    private User findByCredentials(UserCredentialsDto userCredentialsDto) throws BadCredentialsException {
        Optional<User> optionalUser = userRepository.findByEmail(userCredentialsDto.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(userCredentialsDto.getPassword(), user.getPassword())) {
                return user;
            }
        }
        throw new BadCredentialsException("Email or Password is not correct");
    }
}
