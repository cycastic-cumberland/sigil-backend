package net.cycastic.sigil.application.tenant.members.invite;


import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.application.validation.JakartaValidationHelper;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.tenant.UserStatus;
import net.cycastic.sigil.domain.repository.tenant.TenantUserRepository;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompleteTenantInvitationCommandHandler implements Command.Handler<CompleteTenantInvitationCommand, Void> {
    private final UserRepository userRepository;
    private final UserService userService;
    private final TenantUserRepository tenantUserRepository;

    @Override
    public Void handle(CompleteTenantInvitationCommand command) {
        var user = userRepository.findById(command.getQueryParams().getUserId())
                .stream()
                .filter(u -> u.getStatus() != UserStatus.DISABLED)
                .findFirst()
                .orElseThrow(() -> new RequestException(404, "User not found"));
        var tenantUser = tenantUserRepository.findById(command.getQueryParams().getTenantUserId())
                .orElseThrow(() -> new RequestException(400, "Request invalidated"));
        if (user.getStatus().equals(UserStatus.INVITED)){
            JakartaValidationHelper.validateObject(command.getForm());
            userService.completeRegistrationNoTransaction(user, command.getForm());
        }

        tenantUser.setLastInvited(null);
        tenantUserRepository.save(tenantUser);
        userService.invalidateAllUserAuthCache(user);
        return null;
    }
}
