package com.zerotrust.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenValidator {

    private final JwtTokenProvider tokenProvider;

    public JwtTokenValidator(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public ValidationResult validate(String token) {
        if (token == null || token.isBlank()) {
            return ValidationResult.invalid("Token is missing");
        }
        try {
            if (!tokenProvider.isTokenValid(token)) {
                return ValidationResult.invalid("Token is invalid or expired");
            }
            return ValidationResult.valid(tokenProvider.extractEmail(token));
        } catch (ExpiredJwtException e) {
            return ValidationResult.invalid("Token has expired");
        } catch (JwtException e) {
            return ValidationResult.invalid("Token validation failed: " + e.getMessage());
        }
    }

    public record ValidationResult(boolean valid, String email, String error) {
        static ValidationResult valid(String email)    { return new ValidationResult(true,  email, null);   }
        static ValidationResult invalid(String reason) { return new ValidationResult(false, null,  reason); }
    }
}
