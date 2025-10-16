package com.example.bankcards.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private Access access = new Access();
    private Refresh refresh = new Refresh();

    @Getter @Setter
    public static class Access {
        private long expiration;
    }

    @Getter @Setter
    public static class Refresh {
        private long expiration;
    }
}
