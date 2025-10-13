package net.cycastic.sigil.application.user.register;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.cache.EvictCacheBackgroundJob;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.application.user.get.GetKdfSettingsCommand;
import net.cycastic.sigil.configuration.cache.CacheConfigurations;
import net.cycastic.sigil.domain.dto.auth.AuthenticationMethod;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.tenant.UserStatus;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.service.job.JobScheduler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class CompleteUserRegistrationCommandHandler implements Command.Handler<CompleteUserRegistrationCommand, Void> {
    private static final AuthenticationMethod[] AUTHENTICATION_METHODS;

    static {
        var authMethods = AuthenticationMethod.values();
        var methods = new AuthenticationMethod[authMethods.length + 1];
        methods[0] = null;
        System.arraycopy(authMethods, 0, methods, 1, authMethods.length);
        AUTHENTICATION_METHODS = methods;
    }

    private final UserRepository userRepository;
    private final UserService userService;
    private final JobScheduler jobScheduler;

    @Override
    public Void handle(CompleteUserRegistrationCommand command) {
        var user = userRepository.findById(command.getQueryParams().getUserId())
                .stream()
                .filter(u -> u.getStatus() != UserStatus.DISABLED)
                .findFirst()
                .orElseThrow(() -> new RequestException(404, "User not found"));

        if (user.isEmailVerified()){
            throw new RequestException(400, "User has completed registration");
        }
        if (!Arrays.equals(Base64.getDecoder().decode(command.getQueryParams().getSecurityStamp()), user.getSecurityStamp())){
            throw RequestException.forbidden();
        }

        userService.completeRegistrationNoTransaction(user, command.getForm());
        // Deter brute force
        for (var method : AUTHENTICATION_METHODS){
            jobScheduler.deferInfallible(EvictCacheBackgroundJob.builder()
                    .cacheName(CacheConfigurations.Presets.LONG_LIVE_CACHE)
                    .cacheKey(String.format("AuthController::getKdfSettings?command=%s", GetKdfSettingsCommand.builder()
                            .userEmail(user.getNormalizedEmail())
                            .method(method)
                            .build()))
                    .build());
        }
        return null;
    }
}
