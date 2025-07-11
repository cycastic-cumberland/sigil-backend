package net.cycastic.sigil.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.model.Tenant;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDto {
    @NotNull
    private Integer id;

    private String tenantName;

    @NotNull
    private long accumulatedAttachmentStorageUsage;

    @NotNull
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime updatedAt;

    private OffsetDateTime removedAt;

    public static TenantDto fromDomain(Tenant tenant){
        return TenantDto.builder()
                .id(tenant.getId())
                .tenantName(tenant.getName())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .removedAt(tenant.getRemovedAt())
                .accumulatedAttachmentStorageUsage(tenant.getAccumulatedAttachmentStorageUsage())
                .build();
    }
}
