package net.cycastic.sigil.controller.pm;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.task.status.get.GetTaskStatusesByBoardIdCommand;
import net.cycastic.sigil.application.pm.task.status.save.SaveTaskStatusCommand;
import net.cycastic.sigil.application.pm.task.status.transit.connect.SaveTaskStatusProgressionCommand;
import net.cycastic.sigil.controller.annotation.RequirePartitionId;
import net.cycastic.sigil.controller.annotation.RequireTenantId;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.pm.TaskStatusesDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequireTenantId
@RequirePartitionId
@RequiredArgsConstructor
@RequestMapping("api/pm/tasks/statuses")
public class TaskStatusesController {
    private final Pipelinr pipelinr;

    @PostMapping
    public IdDto saveStatus(@Valid @RequestBody SaveTaskStatusCommand command){
        return pipelinr.send(command);
    }

    @GetMapping
    public TaskStatusesDto getStatuses(@Valid GetTaskStatusesByBoardIdCommand command){
        return pipelinr.send(command);
    }

    @PostMapping("connections")
    public void saveConnections(@Valid @RequestBody SaveTaskStatusProgressionCommand command){
        pipelinr.send(command);
    }
}
