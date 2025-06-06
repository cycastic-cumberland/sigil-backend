package net.cycastic.portfoliotoolkit.service;

import org.springframework.security.crypto.password.PasswordEncoder;

public interface PasswordHasher extends PasswordEncoder {
    String hash(String password);
    boolean verify(String input, String hashedPassword);

    default String encode(CharSequence rawPassword){
        return hash(rawPassword.toString());
    }

    default boolean matches(CharSequence rawPassword, String encodedPassword){
        return verify(rawPassword.toString(), encodedPassword);
    }
}
