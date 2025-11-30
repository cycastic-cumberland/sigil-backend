package net.cycastic.sigil.controller.pm;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.task.status.delete.DeleteTaskStatusCommand;
import net.cycastic.sigil.application.pm.task.status.get.GetTaskStatusesByBoardIdCommand;
import net.cycastic.sigil.application.pm.task.status.save.SaveTaskStatusCommand;
import net.cycastic.sigil.application.pm.task.status.transit.connect.SaveTaskStatusProgressionCommand;
import net.cycastic.sigil.configuration.cache.CacheConfigurations;
import net.cycastic.sigil.controller.annotation.RequirePartitionId;
import net.cycastic.sigil.controller.annotation.RequireTenantId;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.pm.TaskStatusesDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequireTenantId
@RequirePartitionId
@RequiredArgsConstructor
@RequestMapping("api/pm/tasks/statuses")
public class TaskStatusesController {
    private static final String CACHE_KEY = "TaskStatusesController";
    private final Pipelinr pipelinr;

    @PostMapping
    @CacheEvict(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'getStatuses' + '?partitionId=' + #partitionId + '&boardId=' + #command.kanbanBoardId",
            condition = "#partitionId != null")
    public IdDto saveStatus(@RequestHeader(ApplicationConstants.PARTITION_ID_HEADER) String partitionId,
                            @Valid @RequestBody SaveTaskStatusCommand command){
        ApplicationUtilities.deoptimize(partitionId);
        return pipelinr.send(command);
    }

    @GetMapping
    @Cacheable(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'getStatuses' + '?partitionId=' + #partitionId + '&boardId=' + #command.kanbanBoardId",
            condition = "#partitionId != null")
    public TaskStatusesDto getStatuses(@RequestHeader(ApplicationConstants.PARTITION_ID_HEADER) String partitionId,
                                       @Valid GetTaskStatusesByBoardIdCommand command){
        ApplicationUtilities.deoptimize(partitionId);
        return pipelinr.send(command);
    }

    @DeleteMapping
    @CacheEvict(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'getStatuses' + '?partitionId=' + #partitionId + '&boardId=' + #command.kanbanBoardId",
            condition = "#partitionId != null")
    public void deleteStatus(@RequestHeader(ApplicationConstants.PARTITION_ID_HEADER) String partitionId,
                             @Valid DeleteTaskStatusCommand command) {
        ApplicationUtilities.deoptimize(partitionId);
        pipelinr.send(command);
    }

    @PostMapping("connections")
    @CacheEvict(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'getStatuses' + '?partitionId=' + #partitionId + '&boardId=' + #command.kanbanBoardId",
            condition = "#partitionId != null")
    public void saveConnections(@RequestHeader(ApplicationConstants.PARTITION_ID_HEADER) String partitionId,
                                @Valid @RequestBody SaveTaskStatusProgressionCommand command){
        ApplicationUtilities.deoptimize(partitionId);
        pipelinr.send(command);
    }
}
