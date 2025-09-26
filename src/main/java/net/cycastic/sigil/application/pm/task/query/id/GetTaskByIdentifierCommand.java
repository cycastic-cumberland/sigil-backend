package net.cycastic.sigil.application.pm.task.query.id;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import net.cycastic.sigil.domain.dto.pm.TaskDto;

@Data
public class GetTaskByIdentifierCommand implements Command<TaskDto> {
    @NotBlank
    private String taskId;
}
