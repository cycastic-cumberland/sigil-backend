package net.cycastic.sigil.auth;

import net.cycastic.sigil.service.auth.JwtIssuer;
import net.cycastic.sigil.service.auth.JwtVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class JwtTests {
    private final JwtIssuer jwtIssuer;
    private final JwtVerifier jwtVerifier;

    @Autowired
    public JwtTests(JwtIssuer jwtIssuer, JwtVerifier jwtVerifier) {
        this.jwtIssuer = jwtIssuer;
        this.jwtVerifier = jwtVerifier;
    }

    @Test
    public void simpleVerificationTest(){
        var token = jwtIssuer.generateTokens("test", null);
        jwtVerifier.extractClaims(token);
        jwtIssuer.refreshToken(token);
    }
}
