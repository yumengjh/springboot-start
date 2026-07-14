package com.yumg.starter.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.yumg.starter.entities.User;
import com.yumg.starter.modules.auth.infrastructure.UserRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class TokenVersionJwtValidatorTest {

    @Test
    void rejectsJwtWhoseTokenVersionNoLongerMatchesTheUser() {
        UserRepository users = org.mockito.Mockito.mock(UserRepository.class);
        User user = new User("alice", "Alice", "hash");
        user.invalidateSessions();
        when(users.findById("user-1")).thenReturn(Optional.of(user));

        var result = new TokenVersionJwtValidator(users).validate(jwt(0));

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).anyMatch(error -> error.getErrorCode().equals("invalid_token"));
    }

    @Test
    void acceptsJwtWhoseTokenVersionMatchesAnActiveUser() {
        UserRepository users = org.mockito.Mockito.mock(UserRepository.class);
        User user = new User("alice", "Alice", "hash");
        when(users.findById("user-1")).thenReturn(Optional.of(user));

        assertThat(new TokenVersionJwtValidator(users).validate(jwt(0)).hasErrors()).isFalse();
    }

    private Jwt jwt(long tokenVersion) {
        Instant now = Instant.now();
        return new Jwt("token", now, now.plusSeconds(60), Map.of("alg", "RS256"),
                Map.of("sub", "user-1", "token_version", tokenVersion));
    }
}
