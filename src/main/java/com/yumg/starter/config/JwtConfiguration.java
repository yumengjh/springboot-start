package com.yumg.starter.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import com.yumg.starter.modules.auth.infrastructure.UserRepository;

@Configuration(proxyBeanMethods = false)
public class JwtConfiguration {
    @Bean
    KeyPair jwtKeyPair(@Value("${app.jwt.private-key-pem:}") String privateKeyPem,
                       @Value("${app.jwt.public-key-pem:}") String publicKeyPem,
                       @Value("${app.jwt.private-key-path:}") String privateKeyPath,
                       @Value("${app.jwt.public-key-path:}") String publicKeyPath,
                       @Value("${app.jwt.allow-ephemeral-key:true}") boolean allowEphemeralKey) throws Exception {
        String privateMaterial = material(privateKeyPem, privateKeyPath);
        String publicMaterial = material(publicKeyPem, publicKeyPath);
        if (!privateMaterial.isBlank() || !publicMaterial.isBlank()) {
            if (privateMaterial.isBlank() || publicMaterial.isBlank()) {
                throw new IllegalStateException("Both APP_JWT_PRIVATE_KEY_* and APP_JWT_PUBLIC_KEY_* must be configured");
            }
            KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(decodePem(privateMaterial)));
            RSAPublicKey publicKey = (RSAPublicKey) factory.generatePublic(new X509EncodedKeySpec(decodePem(publicMaterial)));
            return new KeyPair(publicKey, privateKey);
        }
        if (!allowEphemeralKey) {
            throw new IllegalStateException("RSA JWT keys are required outside local development");
        }
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    @Bean
    JwtEncoder jwtEncoder(KeyPair jwtKeyPair) {
        RSAKey key = new RSAKey.Builder((RSAPublicKey) jwtKeyPair.getPublic())
                .privateKey((RSAPrivateKey) jwtKeyPair.getPrivate()).keyID("local").build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
    }

    @Bean
    JwtDecoder jwtDecoder(KeyPair jwtKeyPair, UserRepository users) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) jwtKeyPair.getPublic()).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), new TokenVersionJwtValidator(users)));
        return decoder;
    }

    private String material(String pem, String path) throws Exception {
        if (pem != null && !pem.isBlank()) return pem.replace("\\n", "\n");
        return path == null || path.isBlank() ? "" : Files.readString(Path.of(path));
    }

    private byte[] decodePem(String value) {
        return Base64.getMimeDecoder().decode(value.replaceAll("-----BEGIN [^-]+-----|-----END [^-]+-----|\\s", ""));
    }
}
