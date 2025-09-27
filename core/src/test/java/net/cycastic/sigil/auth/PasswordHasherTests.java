package net.cycastic.sigil.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class PasswordHasherTests {
    private final PasswordEncoder passwordHasher;

    @Autowired
    public PasswordHasherTests(PasswordEncoder passwordHasher) {
        this.passwordHasher = passwordHasher;
    }

    @Test
    public void simpleHashCheck(){
        final var password = "AbcTestSample1234!@#";
        final var wrongPassword = "Oops! Wrong password!";
        var hash = passwordHasher.encode(password);
        assertTrue(passwordHasher.matches(password, hash));
        assertFalse(passwordHasher.matches(wrongPassword, hash));
    }
}
