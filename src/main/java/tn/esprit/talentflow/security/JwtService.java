package tn.esprit.talentflow.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY =
            "TalentFlowSecretKeyForJwtAuthentication2026";

    private static final SecretKey key = Keys.hmacShaKeyFor(
            SECRET_KEY.getBytes(StandardCharsets.UTF_8)
    );

    private static final long expiration = 1000 * 60 * 60; // 1 heure

    public static String generateToken(String email) {

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }
    public boolean isTokenValid( String jwt, UserDetails userDetails) {
        final String username = extractUsername(jwt);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(jwt);
    }
    private boolean isTokenExpired(String jwt) {
        Date expirationDate = Jwts.parser()
                .verifyWith(key)
                .build() .parseSignedClaims(jwt)
                .getPayload() .getExpiration();
        return expirationDate.before(new Date());
    }
    public String extractUsername(String jwt) {
        return Jwts.parser()
                .verifyWith(key)
                .build() .parseSignedClaims(jwt)
                .getPayload()
                .getSubject();
    }
}
