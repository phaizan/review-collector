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

    //TODO добавить проверку на expire и подделку (try?)

    private final Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode("bXktdmVyeS1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gh"));

    private final long EXPIRATION_TIME = 1000 * 60 * 60;

    public String generateToken(User user) {

        return Jwts.builder()
                .setSubject(user.getLogin())
                .claim("name", user.getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

    }

    public String extractLogin(String token) {
        token = token.replace("Bearer ", "");
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

}
