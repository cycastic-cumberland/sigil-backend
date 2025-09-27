package net.cycastic.sigil.service.impl;

import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.PasswordValidator;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class PasswordValidatorImpl implements PasswordValidator {
    private static final Pattern PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    @Override
    public void validate(String password) {
        if (!PATTERN.matcher(password).matches()){
            throw new RequestException(400, "Password must have at least eight characters, one uppercase letter, one lowercase letter, one number and one special character");
        }
    }
}
