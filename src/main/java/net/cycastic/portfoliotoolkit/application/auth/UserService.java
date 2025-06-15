package net.cycastic.portfoliotoolkit.application.auth;

import net.cycastic.portfoliotoolkit.domain.model.User;

import java.security.SecureRandom;

public class UserService {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static void refreshSecurityStamp(User user){
        RANDOM.nextBytes(user.getSecurityStamp());
    }
}
