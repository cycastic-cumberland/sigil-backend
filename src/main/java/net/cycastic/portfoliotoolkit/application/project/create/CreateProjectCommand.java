package net.cycastic.portfoliotoolkit.application.project.create;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import net.cycastic.portfoliotoolkit.dto.IdDto;

public record CreateProjectCommand(int userId, @NotNull String projectName, String corsSettings) implements Command<IdDto> {
}
