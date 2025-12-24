package net.cycastic.sigil.controller.pm;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.task.create.CreateTaskCommand;
import net.cycastic.sigil.application.pm.task.query.board.QueryTasksByKanbanBoardCommand;
import net.cycastic.sigil.application.pm.task.query.id.GetTaskByIdentifierCommand;
import net.cycastic.sigil.application.pm.task.transit.MoveTasksCommand;
import net.cycastic.sigil.application.pm.task.update.UpdateTaskCommand;
import net.cycastic.sigil.configuration.cache.CacheConfigurations;
import net.cycastic.sigil.controller.annotation.RequirePartitionId;
import net.cycastic.sigil.controller.annotation.RequireTenantId;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.pm.TaskCardsDto;
import net.cycastic.sigil.domain.dto.pm.TaskDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.*;

@RestController
@RequireTenantId
@RequiredArgsConstructor
@RequestMapping("api/pm/tasks")
public class TasksController {
    private static final String CACHE_KEY = "TasksController";
    private final Pipelinr pipelinr;

    @PostMapping
    @RequirePartitionId
    @CacheEvict(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'queryTasksByBoard' + '?tenantId' + #tenantId + '&kanbanBoardId=' + #command.kanbanBoardId",
            condition = "#command.kanbanBoardId != null")
    public IdDto createTask(@RequestHeader(ApplicationConstants.TENANT_ID_HEADER) String tenantId,
                            @Valid @RequestBody CreateTaskCommand command){
        ApplicationUtilities.deoptimize(tenantId);
        return pipelinr.send(command);
    }

    @PatchMapping
    @RequirePartitionId
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
                    key = "'queryTasksByBoard' + '?tenantId' + #tenantId + '&kanbanBoardId=' + #command.kanbanBoardId",
                    condition = "#command.kanbanBoardId != null"),
            @CacheEvict(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
                    key = "'getExactTask' + '?tenantId' + #tenantId + '&taskId=' + #command.taskId")
    })
    public void updateTask(@RequestHeader(ApplicationConstants.TENANT_ID_HEADER) String tenantId,
                           @Valid @RequestBody UpdateTaskCommand command){
        ApplicationUtilities.deoptimize(tenantId);
        pipelinr.send(command);
    }

    @PatchMapping("move")
    @RequirePartitionId
    @CacheEvict(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'queryTasksByBoard' + '?tenantId' + #tenantId + '&kanbanBoardId=' + #command.kanbanBoardId",
            condition = "#command.kanbanBoardId != null")
    public void moveTask(@RequestHeader(ApplicationConstants.TENANT_ID_HEADER) String tenantId,
                         @Valid @RequestBody MoveTasksCommand command){
        ApplicationUtilities.deoptimize(tenantId);
        pipelinr.send(command);
    }

    @GetMapping("task")
    @Cacheable(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'getExactTask' + '?tenantId' + #tenantId + '&taskId=' + #command.taskId")
    public TaskDto getExactTask(@RequestHeader(ApplicationConstants.TENANT_ID_HEADER) String tenantId,
                                @Valid GetTaskByIdentifierCommand command){
        ApplicationUtilities.deoptimize(tenantId);
        return pipelinr.send(command);
    }

    @GetMapping("by-board")
    @RequirePartitionId
    @Cacheable(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'queryTasksByBoard' + '?tenantId' + #tenantId + '&kanbanBoardId=' + #command.kanbanBoardId")
    public TaskCardsDto queryTasksByBoard(@RequestHeader(ApplicationConstants.TENANT_ID_HEADER) String tenantId,
                                          @Valid QueryTasksByKanbanBoardCommand command){
        ApplicationUtilities.deoptimize(tenantId);
        return pipelinr.send(command);
    }
}
