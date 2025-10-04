package net.cycastic.sigil.controller.pm;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.task.create.CreateTaskCommand;
import net.cycastic.sigil.application.pm.task.query.board.QueryTasksByKanbanBoardCommand;
import net.cycastic.sigil.application.pm.task.query.id.GetTaskByIdentifierCommand;
import net.cycastic.sigil.application.pm.task.transit.MoveTaskCommand;
import net.cycastic.sigil.application.pm.task.update.UpdateTaskCommand;
import net.cycastic.sigil.controller.annotation.RequirePartitionId;
import net.cycastic.sigil.controller.annotation.RequireTenantId;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.pm.TaskCardsDto;
import net.cycastic.sigil.domain.dto.pm.TaskDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequireTenantId
@RequiredArgsConstructor
@RequestMapping("api/pm/tasks")
public class TasksController {
    private final Pipelinr pipelinr;

    @PostMapping
    @RequirePartitionId
    public IdDto createTask(@Valid @RequestBody CreateTaskCommand command){
        return pipelinr.send(command);
    }

    @PatchMapping
    @RequirePartitionId
    public void updateTask(@Valid @RequestBody UpdateTaskCommand command){
        pipelinr.send(command);
    }

    @PatchMapping("move")
    @RequirePartitionId
    public void moveTask(@Valid @RequestBody MoveTaskCommand command){
        pipelinr.send(command);
    }

    @GetMapping("task")
    public TaskDto getExactTask(@Valid GetTaskByIdentifierCommand command){
        return pipelinr.send(command);
    }

    @GetMapping("by-board")
    @RequirePartitionId
    public TaskCardsDto queryTasksByBoard(@Valid QueryTasksByKanbanBoardCommand command){
        return pipelinr.send(command);
    }
}
