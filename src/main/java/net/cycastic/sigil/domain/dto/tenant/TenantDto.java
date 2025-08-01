package net.cycastic.sigil.domain.dto.tenant;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.model.tenant.Tenant;
import net.cycastic.sigil.domain.model.tenant.UsageType;
import net.cycastic.sigil.domain.repository.tenant.TenantRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDto {
    @NotNull
    private Integer id;

    @NotNull
    private String tenantName;

    @NotNull
    private long accumulatedAttachmentStorageUsage;

    @NotNull
    private TenantMembership membership;

    @NotNull
    private UsageType usageType;

    @NotNull
    private List<String> permissions;

    @NotNull
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime updatedAt;

    public static TenantDto fromDomain(Tenant tenant){
        return TenantDto.builder()
                .id(tenant.getId())
                .tenantName(tenant.getName())
                .accumulatedAttachmentStorageUsage(tenant.getAccumulatedAttachmentStorageUsage())
                .membership(TenantMembership.MEMBER)
                .usageType(tenant.getUsageType())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }

    public static TenantDto fromDomain(TenantRepository.TenantQueryItem tenant){
        return TenantDto.builder()
                .id(tenant.getId())
                .tenantName(tenant.getTenantName())
                .accumulatedAttachmentStorageUsage(tenant.getAccumulatedAttachmentStorageUsage())
                .membership(switch (tenant.getMembership()){
                    case 0 -> TenantMembership.OWNER;
                    case 1 -> TenantMembership.MODERATOR;
                    default -> TenantMembership.MEMBER;
                 })
                .usageType(tenant.getUsageType())
                .permissions(ApplicationConstants.TenantPermissions.toReadablePermissions(tenant.getPermissions()))
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
