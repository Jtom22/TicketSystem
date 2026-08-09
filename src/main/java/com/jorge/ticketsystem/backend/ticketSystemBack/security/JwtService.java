package com.jorge.ticketsystem.backend.ticketSystemBack.security;

 
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
 
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
 
@Service
public class JwtService {
 
    // Se leen de application.properties (o de variables de entorno)
    // NUNCA hardcodear el secret directamente en el código: si el repo es
    // público, cualquiera podría firmar tokens válidos 
    @Value("${jwt.secret}")
    private String secretKey;
 
    @Value("${jwt.expiration-ms}")
    private long expirationMs;
 
    //Convierte la cadena secretKey a un array de bytes y genera una clave criptográfica HMAC-SHA apta para firmar y verificar tokens de manera segura
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    //Método recibe un objeto UserDetails (el usuario autenticado por Spring Security) y devuelve el token JWT generado como un String
    public String generateToken(UserDetails userDetails) {

        //sacamos que roles maneja este usuario
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
 
        return Jwts.builder()
                .subject(userDetails.getUsername()) // aquí guardamos el email
                .claim("roles", roles)//añadimos los roles
                .issuedAt(new Date())//fecha y hora exactas en las que se creó el token.
                .expiration(new Date(System.currentTimeMillis() + expirationMs))//establece cuando se expira el token
                .signWith(getSigningKey())//Firma digitalmente el contenido del token usando la clave secreta
                .compact();//Ensambla las tres partes del JWT (encabezado, contenido y firma) en una única cadena de texto codificada en Base64
    }
 
    //Método que recibe un token y recupera el valor del campo Subject
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
 
    // Valida si el token es legítimo verificando que el usuario extraído coincida con el usuario de la base de datos y que el token no haya expirado
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
 
    //Comprueba si la fecha de caducidad del token es anterior a la fecha y hora actual
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
 
    //Método genérico privado que permite leer cualquier dato específico de las demandas
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}
