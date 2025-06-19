package net.cycastic.portfoliotoolkit.application.email.smtp.save;

import net.cycastic.portfoliotoolkit.application.validation.CommandValidator;
import net.cycastic.portfoliotoolkit.domain.dto.IdDto;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class SaveSmtpCredentialCommandValidator implements CommandValidator<SaveSmtpCredentialCommand, IdDto> {
    private static final Set<String> VALID_SECURITY_SETTINGS;
    private static final Pattern HOSTNAME_PATTERN;
    private static final Pattern EMAIL_PATTERN;

    static {
        HOSTNAME_PATTERN = Pattern.compile(
                "^(?=.{1,253}$)(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\\.(?!-)[A-Za-z0-9-]{1,63}(?<!-))*\\.?$"
        );

        EMAIL_PATTERN = Pattern.compile(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        );

        VALID_SECURITY_SETTINGS = new HashSet<>();
        VALID_SECURITY_SETTINGS.add("starttls");
        VALID_SECURITY_SETTINGS.add("none");
    }

    @Override
    public void validate(SaveSmtpCredentialCommand command) {
        if (command.getPort() <= 0 || command.getPort() >= 65536){
            throw new RequestException(400, "Port must be between 1 and 65535");
        }
        if (command.getTimeout() <= 0){
            throw new RequestException(400, "Timeout must be greater than 0");
        }
        var secureSmtp = command.getSecureSmtp().toLowerCase(Locale.ROOT);
        if (!VALID_SECURITY_SETTINGS.contains(secureSmtp)){
            throw new RequestException(400, "Invalid security settings");
        }
        if (!HOSTNAME_PATTERN.matcher(command.getServerAddress()).matches()){
            throw new RequestException(400, "Invalid server address");
        }
        if (!EMAIL_PATTERN.matcher(command.getFromAddress()).matches()){
            throw new RequestException(400, "Invalid sender address");
        }
    }
}
