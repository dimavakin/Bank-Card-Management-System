package com.example.bankcards.security.jwt;

import com.example.bankcards.config.JwtProperties;
import com.example.bankcards.dto.JwtAuthenticationDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Setter
@RequiredArgsConstructor
public class JwtService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties jwtProperties;

    public JwtAuthenticationDto generateAuthToken(String email, Collection<? extends GrantedAuthority> authorities) {
        JwtAuthenticationDto jwtDto = new JwtAuthenticationDto();
        jwtDto.setToken(generateJwtToken(email, authorities));
        jwtDto.setRefreshToken(generateRefreshToken(email));
        return jwtDto;
    }

    public JwtAuthenticationDto refreshBaseToken(String email,
                                                 String refreshToken,
                                                 Collection<? extends GrantedAuthority> authorities) {
        JwtAuthenticationDto jwtDto = new JwtAuthenticationDto();
        jwtDto.setToken(generateJwtToken(email, authorities));
        jwtDto.setRefreshToken(refreshToken);
        return jwtDto;
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSingInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSingInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (ExpiredJwtException e) {
            LOGGER.error("Expired JwtException", e);
        } catch (UnsupportedJwtException e) {
            LOGGER.error("Unsupported JwtException", e);
        } catch (MalformedJwtException e) {
            LOGGER.error("Malformed JwtException", e);
        } catch (SecurityException e) {
            LOGGER.error("Security Exception", e);
        } catch (Exception e) {
            LOGGER.error("Invalid token", e);
        }
        return false;
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSingInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("roles", List.class);
    }

    private String generateRefreshToken(String email) {
        Date date = Date.from(LocalDateTime.now().plusDays(jwtProperties.getRefresh().getExpiration()).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .subject(email)
                .expiration(date)
                .signWith(getSingInKey())
                .compact();
    }

    private String generateJwtToken(String email, Collection<? extends GrantedAuthority> authorities) {
        Date date = Date.from(LocalDateTime.now().plusMinutes(jwtProperties.getAccess().getExpiration()).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .subject(email)
                .claim("roles", authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .expiration(date)
                .signWith(getSingInKey())
                .compact();
    }

    private SecretKey getSingInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
