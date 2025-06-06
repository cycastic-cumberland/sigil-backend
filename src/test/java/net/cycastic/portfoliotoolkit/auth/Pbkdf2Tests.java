package net.cycastic.portfoliotoolkit.auth;

import net.cycastic.portfoliotoolkit.service.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class Pbkdf2Tests {
    private final PasswordHasher passwordHasher;

    @Autowired
    public Pbkdf2Tests(PasswordHasher passwordHasher) {
        this.passwordHasher = passwordHasher;
    }

    @Test
    public void simpleHashCheck(){
        final var password = "AbcTestSample1234!@#";
        final var wrongPassword = "Oops! Wrong password!";
        var hash = passwordHasher.hash(password);
        assert passwordHasher.verify(password, hash);
        assert !passwordHasher.verify(wrongPassword, hash);
    }
}
