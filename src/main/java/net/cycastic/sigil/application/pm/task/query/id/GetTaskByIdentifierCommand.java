package net.cycastic.sigil.application.pm.task.query.id;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.domain.dto.pm.TaskDto;

@Data
public class GetTaskByIdentifierCommand implements Command<TaskDto> {
    @NotEmpty
    private String taskId;
}
