package net.cycastic.sigil.application.project.delete;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteProjectCommand implements Command<@Null Object> {
    private int id;
}
