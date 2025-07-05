package net.cycastic.sigil.application.tenant.delete;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteTenantCommand implements Command<@Null Object> {
    private int id;
}
