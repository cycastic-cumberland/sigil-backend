package net.cycastic.sigil.application.admin.entitlement.list;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.Data;
import net.cycastic.sigil.application.user.validation.admin.RequireAdmin;
import net.cycastic.sigil.domain.dto.EntitlementDto;
import net.cycastic.sigil.domain.dto.paging.EnumerablePage;

@Data
@RequireAdmin
public class ListEntitlementsCommand implements Command<EnumerablePage<EntitlementDto>> {
    @Nullable
    private String paginationToken;

    @Min(1)
    @Nullable
    private Integer pageSize;
}
