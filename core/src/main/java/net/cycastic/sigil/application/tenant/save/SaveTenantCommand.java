package net.cycastic.sigil.application.tenant.save;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.model.tenant.UsageType;
import jakarta.annotation.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TransactionalCommand
public class SaveTenantCommand implements Command<IdDto> {
    private @Nullable Integer id;
    private @Nullable Integer userId;
    private @NotNull String tenantName;
    private @Nullable UsageType usageType;
}
