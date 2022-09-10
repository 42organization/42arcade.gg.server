package io.pp.arcade.domain.security.oauth.v2.config.security;

import io.pp.arcade.domain.security.oauth.v2.token.AuthTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    @Value("${jwt.secret}")
    private String secret;

    @Bean("jwtBean")
    public AuthTokenProvider jwtProvider() {
        return new AuthTokenProvider(secret);
    }
}