package net.cycastic.sigil.controller.pm;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.kanban.query.id.GetKanbanBoardByIdCommand;
import net.cycastic.sigil.application.pm.kanban.query.project.QueryKanbanBoardByProjectCommand;
import net.cycastic.sigil.application.pm.kanban.save.SaveKanbanBoardCommand;
import net.cycastic.sigil.configuration.cache.CacheConfigurations;
import net.cycastic.sigil.controller.annotation.RequirePartitionId;
import net.cycastic.sigil.controller.annotation.RequireTenantId;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.dto.pm.KanbanBoardDto;
import net.cycastic.sigil.domain.exception.RequestException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.*;

@RestController
@RequireTenantId
@RequirePartitionId
@RequiredArgsConstructor
@RequestMapping("api/pm/kanban")
public class KanbanController {
    private static final String CACHE_KEY = "KanbanController";
    private final Pipelinr pipelinr;

    @PostMapping
    @Caching(evict = {
        @CacheEvict(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
                key = "'queryBoards'+ '?partitionId=' + #partitionId",
                condition = "#partitionId != null"),
        @CacheEvict(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
                key = "'getBoardById'+ '?id=' + #command.id",
                condition = "#command.id != null")
    })
    public IdDto saveBoard(@RequestHeader(ApplicationConstants.PARTITION_ID_HEADER) String partitionId,
                           @Valid @RequestBody SaveKanbanBoardCommand command){
        ApplicationUtilities.deoptimize(partitionId);
        return pipelinr.send(command);
    }

    @GetMapping
    @Cacheable(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'queryBoards'+ '?partitionId=' + #partitionId",
            condition = "#partitionId != null")
    public PageResponseDto<KanbanBoardDto> queryBoards(@RequestHeader(ApplicationConstants.PARTITION_ID_HEADER) String partitionId,
                                                       @Valid QueryKanbanBoardByProjectCommand command){
        ApplicationUtilities.deoptimize(partitionId);
        return pipelinr.send(command);
    }

    @GetMapping("id/{id}")
    @Cacheable(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'getBoardById'+ '?id=' + #id",
            condition = "#id != null")
    public KanbanBoardDto getBoardById(@PathVariable String id){
        return pipelinr.send(new GetKanbanBoardByIdCommand(ApplicationUtilities.tryParseInt(id)
                .orElseThrow(() -> new RequestException(400, "ID is not an integer"))));
    }
}
