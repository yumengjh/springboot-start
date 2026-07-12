package com.yumg.starter.modules.auth.application;

import com.yumg.starter.entities.RefreshSession;
import com.yumg.starter.entities.User;
import com.yumg.starter.modules.auth.api.dto.TokenResponse;
import com.yumg.starter.modules.auth.infrastructure.RefreshSessionRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final JwtEncoder encoder;
    private final RefreshSessionRepository refreshSessions;

    public TokenService(JwtEncoder encoder, RefreshSessionRepository refreshSessions) {
        this.encoder = encoder;
        this.refreshSessions = refreshSessions;
    }

    @Transactional
    public TokenResponse issue(User user) {
        Instant now = Instant.now();
        Instant accessExpiry = now.plus(15, ChronoUnit.MINUTES);
        String rawRefreshToken = newRefreshToken();
        refreshSessions.save(new RefreshSession(user.getId(), UUID.randomUUID().toString(),
                sha256(rawRefreshToken), now, now.plus(30, ChronoUnit.DAYS)));
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("springboot-start").subject(user.getId())
                .issuedAt(now).expiresAt(accessExpiry).id(UUID.randomUUID().toString())
                .claim("username", user.getUsername()).claim("token_version", user.getTokenVersion())
                .build();
        String accessToken = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).build(), claims)).getTokenValue();
        return new TokenResponse(accessToken, rawRefreshToken, "Bearer", 900);
    }

    static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static String newRefreshToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
