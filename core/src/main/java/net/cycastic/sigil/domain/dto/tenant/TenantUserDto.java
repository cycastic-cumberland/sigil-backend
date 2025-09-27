package net.cycastic.sigil.domain.dto.tenant;

import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.repository.tenant.TenantUserRepository;

import java.util.List;

@Data
@Builder
public class TenantUserDto {
    private String firstName;
    private String lastName;
    private String email;
    private TenantMembership membership;
    private List<String> permissions;

    public static TenantUserDto fromDomain(TenantUserRepository.TenantUserItem domain){
        return TenantUserDto.builder()
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .email(domain.getEmail())
                .membership(switch (domain.getMembership()){
                    case 0 -> TenantMembership.OWNER;
                    case 1 -> TenantMembership.MODERATOR;
                    default -> TenantMembership.MEMBER;
                })
                .permissions(ApplicationConstants.TenantPermissions.toReadablePermissions(domain.getPermissions()))
                .build();
    }
}
