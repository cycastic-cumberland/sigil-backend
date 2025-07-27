package net.cycastic.sigil.application.tenant.members.invite;


import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.auth.UserService;
import net.cycastic.sigil.domain.dto.auth.CompleteUserRegistrationForm;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.tenant.UserStatus;
import net.cycastic.sigil.domain.repository.tenant.TenantUserRepository;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class CompleteTenantInvitationCommandHandler implements Command.Handler<CompleteTenantInvitationCommand, Void> {
    private final UserRepository userRepository;
    private final UserService userService;
    private final TenantUserRepository tenantUserRepository;

    @Override
    @Transactional
    public Void handle(CompleteTenantInvitationCommand command) {
        var user = userRepository.findById(command.getQueryParams().getUserId())
                .stream()
                .filter(u -> u.getStatus() != UserStatus.DISABLED)
                .findFirst()
                .orElseThrow(() -> new RequestException(404, "User not found"));
        var tenantUser = tenantUserRepository.findById(command.getQueryParams().getTenantUserId())
                .orElseThrow(() -> new RequestException(400, "Request invalidated"));
        if (user.getStatus().equals(UserStatus.INVITED)){
            if (command.getForm() == null){
                throw new RequestException(400, "Registration form is required");
            }
            try (var factory = Validation.buildDefaultValidatorFactory()){
                var validator = factory.getValidator();
                Set<ConstraintViolation<CompleteUserRegistrationForm>> violations = validator.validate(command.getForm());
                if (!violations.isEmpty()){
                    throw new RequestException(400, String.format("Validation failed: %s",
                            String.join(". ", violations.stream().map(ConstraintViolation::getMessage).toList())));
                }
            }

            userService.completeRegistrationNoTransaction(user, command.getForm());
        }

        tenantUser.setLastInvited(null);
        tenantUserRepository.save(tenantUser);
        return null;
    }
}
