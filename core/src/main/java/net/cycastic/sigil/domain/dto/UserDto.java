package net.cycastic.sigil.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.model.tenant.User;
import net.cycastic.sigil.domain.model.tenant.UserStatus;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private String[] roles;
    private String publicRsaKey;
    private int tenantOwnerCount;
    private boolean hasPasswordCredential;
    private boolean hasWebAuthnCredential;
    private UUID notificationToken;
    private UUID avatarToken;
    private boolean emailVerified;
    private UserStatus status;
    private OffsetDateTime joinedAt;
    private OffsetDateTime updatedAt;

    public static UserDto fromDomain(@NotNull User user){
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().split(","))
                .publicRsaKey(user.getPublicRsaKey() == null ? null : Base64.getEncoder().encodeToString(user.getPublicRsaKey()))
                .hasPasswordCredential(user.getWrappedUserKey() != null)
                .hasWebAuthnCredential(user.getWebAuthnCredential() != null)
                .notificationToken(user.getNotificationToken().getToken())
                .avatarToken(user.getAvatarToken())
                .emailVerified(user.isEmailVerified())
                .status(user.getStatus())
                .joinedAt(user.getJoinedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
