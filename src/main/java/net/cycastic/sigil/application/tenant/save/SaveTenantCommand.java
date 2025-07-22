package net.cycastic.sigil.application.tenant.save;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.IdDto;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveTenantCommand implements Command<IdDto> {
    private @Nullable Integer id;
    private @Nullable Integer userId;
    private @NotNull String tenantName;
}
