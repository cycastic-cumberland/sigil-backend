package net.cycastic.sigil.domain.dto.pm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;
import net.cycastic.sigil.domain.model.pm.Task;
import net.cycastic.sigil.domain.model.pm.TaskPriority;

import java.time.OffsetDateTime;
import java.util.Base64;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCardDto {
    private int id;
    private String taskIdentifier;
    private Long taskStatusId;
    private Integer kanbanBoardId;
    private Integer assigneeId;
    private Integer reporterId;
    private UserDto assignee;
    private TaskPriority priority;
    private CipherDto encryptedName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    protected static <C extends TaskCardDto, B extends TaskCardDtoBuilder<C, B>> B fromDomain(Task task, TaskCardDtoBuilder<C, B> builder){
        return builder.id(task.getId())
                .taskIdentifier(task.getTaskIdentifier())
                .taskStatusId(task.getTaskStatus() == null ? null : task.getTaskStatus().getId())
                .kanbanBoardId(task.getKanbanBoard() == null ? null : task.getKanbanBoard().getId())
                .assigneeId(task.getAssignee() == null ? null : task.getAssignee().getId())
                .assignee(task.getAssignee() == null ? null : UserDto.fromDomain(task.getAssignee()))
                .reporterId(task.getReporter() == null ? null : task.getReporter().getId())
                .priority(task.getPriority())
                .encryptedName(CipherDto.fromDomain(task.getEncryptedName()))
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt());
    }

    public static TaskCardDto fromDomain(Task domain){
        return fromDomain(domain, TaskCardDto.builder())
                .build();
    }
}
