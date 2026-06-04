package com.project.screenrecorder.Security;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private static SecretKey SIGNING_KEY;

    @Value("${jwt.secret}")
    public void setSecretKey(String secretKey) {
        JwtUtils.SIGNING_KEY = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
    @Value("${jwt.expiry-minutes}")
    private Long expiryMinutes;


    public String generateToken(String token){
        return Jwts.builder()
                .subject(token)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60  * expiryMinutes))
                .signWith(SIGNING_KEY)
                .compact();

    }

    public String extractName(String token){
        return Jwts.parser()
                .verifyWith(SIGNING_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token , String username , UserDetails userDetails){
        return (username.equals(userDetails.getUsername()) &&  ! isTokenExpired(token));
    }


    private boolean isTokenExpired(String token){
        return Jwts.parser()
                .verifyWith(SIGNING_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }




}
