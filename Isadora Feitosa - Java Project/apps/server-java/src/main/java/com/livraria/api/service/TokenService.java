package com.livraria.api.service;

import com.livraria.api.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    // Gera o Token (Já tínhamos)
    public String gerarToken(Usuario usuario) {
        Instant expirationTime = Instant.now().plus(1, ChronoUnit.DAYS);
        return Jwts.builder()
                .setSubject(usuario.getUsername())
                .claim("id", usuario.getId())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(expirationTime))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    // [NOVO] Lê o Token e devolve o Username (se for válido)
    public String getSubject(String tokenJWT) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseClaimsJws(tokenJWT)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            return null; // Token inválido ou expirado
        }
    }
}