package net.cycastic.portfoliotoolkit.application.project.create;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.portfoliotoolkit.dto.IdDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectCommand implements Command<IdDto> {
    private int userId;
    private @NotNull String projectName;
    private @Null String corsSettings;
}
