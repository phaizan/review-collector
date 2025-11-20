package study.reviewscollector.util;

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

    private final long EXPIRATIONTIME = 1000 * 60 * 60;

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getLogin())
                .claim("name", user.getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractLogin(String token) {
        token = token.replace("Bearer ", "");
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

}
