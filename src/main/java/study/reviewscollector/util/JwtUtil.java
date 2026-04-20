package study.reviewscollector.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import study.reviewscollector.entity.User;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode("bXktdmVyeS1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gh"));

    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 12;

    public String generateToken(User user) {

        return Jwts.builder()
                .setSubject(user.getLogin())
                .claim("name", user.getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public String extractLogin(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean validateToken(String token, User user) {
        final String login = extractLogin(token);
        return login.equals(user.getLogin()) && !isTokenExpired(token);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

}
