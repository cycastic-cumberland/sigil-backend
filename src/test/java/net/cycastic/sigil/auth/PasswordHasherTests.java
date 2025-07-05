package net.cycastic.sigil.auth;

import net.cycastic.sigil.service.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class PasswordHasherTests {
    private final PasswordHasher passwordHasher;

    @Autowired
    public PasswordHasherTests(PasswordHasher passwordHasher) {
        this.passwordHasher = passwordHasher;
    }

    @Test
    public void simpleHashCheck(){
        final var password = "AbcTestSample1234!@#";
        final var wrongPassword = "Oops! Wrong password!";
        var hash = passwordHasher.hash(password);
        assertTrue(passwordHasher.verify(password, hash));
        assertFalse(passwordHasher.verify(wrongPassword, hash));
    }
}
