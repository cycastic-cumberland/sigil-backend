package net.cycastic.sigil.application.pm.task.status.get;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Min;
import lombok.Data;
import net.cycastic.sigil.domain.dto.pm.TaskStatusesDto;

@Data
public class GetTaskStatusesByBoardIdCommand implements Command<TaskStatusesDto> {
    @Min(1)
    private int kanbanBoardId;
}
