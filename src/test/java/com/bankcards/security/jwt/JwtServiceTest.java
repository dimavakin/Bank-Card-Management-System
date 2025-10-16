package com.bankcards.security.jwt;

import com.bankcards.config.JwtProperties;
import com.bankcards.dto.JwtAuthenticationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class JwtServiceTest {
    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private JwtProperties.Access access;

    @Mock
    private JwtProperties.Refresh refresh;

    @InjectMocks
    private JwtService jwtService;

    private final String email = "test123@test.com";
    Collection<? extends GrantedAuthority> authorities;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        when(jwtProperties.getSecret()).thenReturn("ss1br4y8HI8xgogPUwjTREKLEl1a8ohYdlXuWAywuyv0TN8FR8KjBGnZQOxstB1NPwDmeJYkka03HmYucOCTs2NqoW0Y2b0ovTJF4HPQMnhbHIqzw1Yl1j6LlHXfrzJD6AuoIzHPQ19dp1EI63J2DgFBRJ1vpPalF37J4fJbf4br9cErpiPjIVscnNmJ3iktQzP87lO6XWQlJ3NUdIkgRsIUSmx6k6UFPqOIqKKsMmM1qIjZANZDMOSx4FNvxQjB");

        when(jwtProperties.getAccess()).thenReturn(access);
        when(jwtProperties.getRefresh()).thenReturn(refresh);


        when(access.getExpiration()).thenReturn(60L);
        when(refresh.getExpiration()).thenReturn(7L);

        jwtService = new JwtService(jwtProperties);
    }

    @Test
    void generateAuthTokenTest() {

        JwtAuthenticationDto jwtAuthenticationDto = jwtService.generateAuthToken(email, authorities);
        assertEquals(email, jwtService.getEmailFromToken(jwtAuthenticationDto.getToken()));
        assertTrue(jwtService.validateJwtToken(jwtAuthenticationDto.getToken()));
    }

    @Test
    void refreshBaseTokenTest() throws InterruptedException {
        JwtAuthenticationDto jwtAuthenticationDto = jwtService.generateAuthToken(email, authorities);

        Thread.sleep(1000);

        JwtAuthenticationDto jwtAuthenticationDtoNew = jwtService.refreshBaseToken(
                email,
                jwtAuthenticationDto.getRefreshToken(),
                authorities
        );

        assertEquals(jwtAuthenticationDto.getRefreshToken(), jwtAuthenticationDtoNew.getRefreshToken());

        assertNotEquals(jwtAuthenticationDtoNew.getToken(), jwtAuthenticationDto.getToken());
    }

}