package net.cycastic.sigil.application.pm.task.create;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.application.tenant.TenantService;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.listing.Partition;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.model.pm.Task;
import net.cycastic.sigil.domain.model.pm.TaskPriority;
import net.cycastic.sigil.domain.model.pm.TaskSubscriber;
import net.cycastic.sigil.domain.repository.CipherRepository;
import net.cycastic.sigil.domain.repository.listing.PartitionRepository;
import net.cycastic.sigil.domain.repository.pm.*;
import net.cycastic.sigil.domain.repository.tenant.TenantUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CreateTaskCommandHandler extends BaseProjectCommandHandler<CreateTaskCommand, IdDto> {
    private final PartitionService partitionService;
    private final PartitionRepository partitionRepository;
    private final TenantService tenantService;
    private final KanbanBoardRepository kanbanBoardRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final TenantUserRepository tenantUserRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserService userService;
    private final TaskRepository taskRepository;
    private final TaskSubscriberRepository taskSubscriberRepository;
    private final ProjectPartitionRepository projectPartitionRepository;
    private final CipherRepository cipherRepository;

    @Override
    protected IdDto handleInternal(CreateTaskCommand command, ProjectPartition projectPartition) {
        var tenant = tenantService.getTenant();
        Partition partition;
        try {
            partition = partitionRepository.getReferenceById(projectPartition.getId());
        } catch (EntityNotFoundException e){
            partition = partitionService.getPartition();
        }

        if (!partition.getTenant().getId().equals(tenant.getId())) {
            throw RequestException.forbidden();
        }

        projectPartition.setLatestTaskId(projectPartition.getLatestTaskId() + 1);
        projectPartitionRepository.save(projectPartition);
        var encryptedName = command.getEncryptedName().toDomain(true);
        var encryptedContent = command.getEncryptedContent() == null ? null : command.getEncryptedContent().toDomain(true);
        cipherRepository.save(encryptedName);
        if (encryptedContent != null){
            cipherRepository.save(encryptedContent);
        }
        var taskBuilder = Task.builder()
                .tenant(tenant)
                .priority(Objects.requireNonNullElse(command.getTaskPriority(), TaskPriority.MEDIUM))
                .label("")
                .taskIdentifier(String.format("%s-%d", projectPartition.getUniqueIdentifier(), projectPartition.getLatestTaskId()))
                .encryptedName(encryptedName)
                .encryptedContent(encryptedContent);
        if (command.getKanbanBoardId() != null){
            var board = kanbanBoardRepository.findByIdAndProjectPartition_Id(command.getKanbanBoardId(), projectPartition.getId())
                    .orElseThrow(() -> new RequestException(400, "Board not found"));
            taskBuilder.kanbanBoard(board);
            if (command.getTaskStatusId() != null){
                var status = taskStatusRepository.findByIdAndKanbanBoard_Id(command.getTaskStatusId(), board.getId())
                        .orElseThrow(() -> new RequestException(404, "Status not found"));
                taskBuilder.taskStatus(status);
            }
        }

        Task task;
        if (tenantUserRepository.existsByTenant_IdAndUser_Id(tenant.getId(), loggedUserAccessor.getUserId())){
            var user = userService.getUser();
            task = taskBuilder.reporter(user)
                    .build();
            var taskSubscriber = TaskSubscriber.builder()
                    .task(task)
                    .subscriber(user)
                    .build();
            taskRepository.save(task);
            taskSubscriberRepository.save(taskSubscriber);
        } else {
            task = taskBuilder.build();
            taskRepository.save(task);
        }

        return new IdDto(task.getTaskIdentifier());
    }
}
