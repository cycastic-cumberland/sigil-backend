package net.cycastic.sigil.application.pm.task.query.backlog;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cycastic.sigil.domain.dto.paging.PageRequestDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.dto.pm.TaskDto;

@Data
@EqualsAndHashCode(callSuper = true)
public class QueryTasksByBacklogCommand extends PageRequestDto implements Command<PageResponseDto<TaskDto>> {
    @Min(1)
    private int partitionId;
}
