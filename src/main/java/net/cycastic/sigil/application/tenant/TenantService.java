package net.cycastic.sigil.application.tenant;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.configuration.application.TenantConfigurations;
import net.cycastic.sigil.domain.ApplicationAssets;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.tenant.Tenant;
import net.cycastic.sigil.domain.model.tenant.TenantUser;
import net.cycastic.sigil.domain.model.tenant.User;
import net.cycastic.sigil.domain.model.tenant.UserStatus;
import net.cycastic.sigil.domain.repository.tenant.TenantRepository;
import net.cycastic.sigil.domain.repository.tenant.TenantUserRepository;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.service.EmailTemplateEngine;
import net.cycastic.sigil.service.LoggedUserAccessor;
import net.cycastic.sigil.service.UrlAccessor;
import net.cycastic.sigil.service.email.EmailTemplates;
import net.cycastic.sigil.service.impl.ApplicationEmailSender;
import net.cycastic.sigil.service.impl.UriPresigner;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantUserRepository tenantUserRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final TenantConfigurations tenantConfigurations;
    private final UriPresigner uriPresigner;
    private final UrlAccessor urlAccessor;
    private final UserService userService;
    private final EmailTemplateEngine emailTemplateEngine;
    private final ApplicationEmailSender applicationEmailSender;

    public int getTenantUserPermissions() {
        var tenantUser = tenantUserRepository.findByTenant_IdAndUser_Id(loggedUserAccessor.getTenantId(), loggedUserAccessor.getUserId())
                .orElseThrow(RequestException::forbidden);
        if (tenantUser.getLastInvited() != null){
            throw RequestException.forbidden();
        }
        return tenantUser.getPermissions();
    }

    public void checkPermission(int mask){
        var tenant = getTenant();
        if (loggedUserAccessor.isAdmin()){
            return;
        }
        if (tenant.getOwner().getId().equals(loggedUserAccessor.getUserId())){
            return;
        }

        if ((getTenantUserPermissions() & mask) != mask){
            throw RequestException.forbidden();
        }
    }

    public Optional<Tenant> tryGetTenant(){
        return loggedUserAccessor.tryGetTenantId()
                .stream()
                .boxed()
                .flatMap(id -> tenantRepository.findById(id).stream())
                .findFirst();
    }

    public Tenant getTenant(){
        return tryGetTenant()
                .orElseThrow(() -> new RequestException(404, "Tenant not found"));
    }

    private URI generateCompletionUri(int userId, int tenantId, int tenantUserId, OffsetDateTime notValidBefore, OffsetDateTime notValidAfter){
        var backendCompletionUri = UriComponentsBuilder.fromUriString(urlAccessor.getBackendOrigin())
                .path("/api/public/tenant/complete-invitation")
                .queryParam("userId", userId)
                .queryParam("tenantId", tenantId)
                .queryParam("tenantUserId", tenantUserId)
                .queryParam("notValidBefore", notValidBefore.toInstant().getEpochSecond())
                .queryParam("notValidAfter", notValidAfter.toInstant().getEpochSecond())
                .build(true)
                .toUri();
        return uriPresigner.signUri(backendCompletionUri);
    }

    @Transactional
    @SneakyThrows
    public void sendTenantInvitation(int tenantId, int inviterId, String userEmail, int permissions){
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId));
        var inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new IllegalStateException("Inviter not found: " + inviterId));
        TenantUser tenantUser;
        var userOpt = userRepository.getByEmail(userEmail.toUpperCase());
        var greeting = "Hi";
        User user;
        if (userOpt.isPresent()){
            user = userOpt.get();
            if (user.getLastName() != null) {
                greeting = String.format("Hi %s", user.getLastName());
            }
            var tenantUserOpt = tenantUserRepository.findByTenant_IdAndUser_Id(tenantId, user.getId());
            if (tenantUserOpt.isPresent()){
                tenantUser = tenantUserOpt.get();
                tenantUser.setPermissions(permissions);
            } else {
                tenantUser = TenantUser.builder()
                        .tenant(tenant)
                        .user(user)
                        .permissions(permissions)
                        .build();
            }
        } else {
            user = userService.registerUserNoTransaction(userEmail,
                    Collections.singleton(ApplicationConstants.Roles.COMMON),
                    UserStatus.INVITED,
                    false);
            tenantUser = TenantUser.builder()
                    .tenant(tenant)
                    .user(user)
                    .permissions(permissions)
                    .build();
        }

        if (tenantUser.getLastInvited() != null){
            var secondsElapsed = Duration.between(tenantUser.getLastInvited(), OffsetDateTime.now()).getSeconds();
            if (secondsElapsed < tenantConfigurations.getResendInvitationLimitSeconds()){
                throw RequestException.withExceptionCode("C400T004", tenantConfigurations.getResendInvitationLimitSeconds() - secondsElapsed);
            }
        }
        tenantUser.setLastInvited(OffsetDateTime.now());
        tenantUserRepository.save(tenantUser);
        var nvb = OffsetDateTime.now();
        var nva = nvb.plusSeconds(tenantConfigurations.getInvitationLinkValidSeconds());
        var backendCompletionUri = generateCompletionUri(user.getId(), tenantId, tenantUser.getId(), nvb, nva);
        var completionUri = UriComponentsBuilder.fromUriString(urlAccessor.getFrontendOrigin())
                .path("/complete-tenant-invitation")
                .queryParam("submission", ApplicationUtilities.encodeURIComponent(backendCompletionUri))
                .build(true)
                .toUri();
        Map<String, Object> parameters = Map.of(
                "completionUri", completionUri,
                "greeting", greeting,
                "tenantName", tenant.getName(),
                "inviterName", String.format("%s %s", inviter.getFirstName(), inviter.getLastName()),
                "__debugBackendCompletionUri", backendCompletionUri,
                "notValidAfter", DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(Locale.ROOT)
                        .format(nva),
                "logo", ApplicationAssets.EmailImages.LOGO_EMAIL_IMAGE
        );

        byte[] renderedContent;
        EmailTemplateEngine.RenderResult renderResult;

        try (var byteStream = new ByteArrayOutputStream()){
            try (var templateStream = EmailTemplates.tenantUserInvitation()){
                renderResult = emailTemplateEngine.render(templateStream, byteStream, parameters);
            }

            renderedContent = byteStream.toByteArray();
        }

        applicationEmailSender.sendHtml(user.getEmail(),
                null,
                String.format("You are invited to \"%s\" tenant", tenant.getName()),
                new String(renderedContent, StandardCharsets.UTF_8),
                renderResult.getImageStreamSource());
    }
}
