package net.cycastic.sigil.controller.pm;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.task.comment.count.CountTaskCommentCommand;
import net.cycastic.sigil.application.pm.task.comment.delete.DeleteTaskCommentCommand;
import net.cycastic.sigil.application.pm.task.comment.query.QueryTaskCommentCommand;
import net.cycastic.sigil.application.pm.task.comment.save.SaveTaskCommentCommand;
import net.cycastic.sigil.configuration.cache.CacheConfigurations;
import net.cycastic.sigil.controller.annotation.RequireTenantId;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.dto.CountDto;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.dto.pm.CommentDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.*;

@RestController
@RequireTenantId
@RequiredArgsConstructor
@RequestMapping("api/pm/tasks/comments")
public class TaskCommentsController {
    private static final String CACHE_KEY = "TaskCommentsController";
    private final Pipelinr pipelinr;

    @PostMapping
    @Caching(
            evict = {
                    @CacheEvict(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
                            key = "'queryComments' + '?tenantId=' + #tenantId + '&taskId=' + #command.taskId + '&page=1&pageSize=10'"),
                    @CacheEvict(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
                            key = "'countComments' + '?tenantId=' + #tenantId + '&taskId=' + #command.taskId",
                            condition = "#command.id == null")
            }
    )
    public IdDto saveComment(@RequestHeader(ApplicationConstants.TENANT_ID_HEADER) String tenantId,
                             @RequestBody @Valid SaveTaskCommentCommand command){
        ApplicationUtilities.deoptimize(tenantId);
        return pipelinr.send(command);
    }

    @DeleteMapping
    @Caching(
            evict = {
                    @CacheEvict(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
                            key = "'queryComments' + '?tenantId=' + #tenantId + '&taskId=' + #command.taskId + '&page=1&pageSize=10'"),
                    @CacheEvict(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
                            key = "'countComments' + '?tenantId=' + #tenantId + '&taskId=' + #command.taskId",
                            condition = "#command.id == null")
            }
    )
    public void deleteComment(@RequestHeader(ApplicationConstants.TENANT_ID_HEADER) String tenantId,
                              @Valid DeleteTaskCommentCommand command){
        ApplicationUtilities.deoptimize(tenantId);
        pipelinr.send(command);
    }

    @GetMapping("count")
    @Cacheable(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'countComments' + '?tenantId=' + #tenantId + '&taskId=' + #command.taskId")
    public CountDto countComments(@RequestHeader(ApplicationConstants.TENANT_ID_HEADER) String tenantId,
                                  @Valid CountTaskCommentCommand command){
        ApplicationUtilities.deoptimize(tenantId);
        return pipelinr.send(command);
    }

    @GetMapping
    @Cacheable(value = CACHE_KEY, cacheManager = CacheConfigurations.CACHE_MANAGER_BEAN_NAME,
            key = "'queryComments' + '?tenantId=' + #tenantId + '&taskId=' + #command.taskId + '&page=1&pageSize=10'",
            condition = "#command.cacheable",
            unless = "!#command.cacheable")
    public PageResponseDto<CommentDto> queryComments(@RequestHeader(ApplicationConstants.TENANT_ID_HEADER) String tenantId,
                                                     @Valid QueryTaskCommentCommand command){
        ApplicationUtilities.deoptimize(tenantId);
        return pipelinr.send(command);
    }
}
