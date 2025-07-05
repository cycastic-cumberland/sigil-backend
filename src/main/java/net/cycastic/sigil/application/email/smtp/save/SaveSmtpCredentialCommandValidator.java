package net.cycastic.sigil.application.email.smtp.save;

import net.cycastic.sigil.application.validation.CommandValidator;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.RequestException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class SaveSmtpCredentialCommandValidator implements CommandValidator<SaveSmtpCredentialCommand, IdDto> {
    private static final Set<String> VALID_SECURITY_SETTINGS;
    private static final Pattern HOSTNAME_PATTERN;

    static {
        HOSTNAME_PATTERN = Pattern.compile(
                "^(?=.{1,253}$)(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\\.(?!-)[A-Za-z0-9-]{1,63}(?<!-))*\\.?$"
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
        if (!ApplicationUtilities.isEmail(command.getFromAddress())){
            throw new RequestException(400, "Invalid sender address");
        }
    }
}
