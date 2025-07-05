package net.cycastic.sigil.application.tenant.get;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.ProjectDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTenantCommand implements Command<ProjectDto> {
    private int id;
}
