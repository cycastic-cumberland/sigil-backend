package net.cycastic.sigil.domain.dto.pm;

import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.domain.model.pm.Task;
import net.cycastic.sigil.domain.model.pm.TaskPriority;

import java.util.Base64;

@Data
@Builder
public class TaskDto {
    private int id;
    private String taskIdentifier;
    private TaskStatusDto taskStatus;
    private UserDto assignee;
    private UserDto reporter;
    private TaskPriority priority;
    private String encryptedName;
    private String encryptedContent;
    private String iv;
    // TODO: Label

    public static TaskDto fromDomain(Task domain){
        return TaskDto.builder()
                .id(domain.getId())
                .taskIdentifier(domain.getTaskIdentifier())
                .taskStatus(domain.getTaskStatus() == null ? null : TaskStatusDto.fromDomain(domain.getTaskStatus()))
                .assignee(domain.getAssignee() == null ? null : UserDto.fromDomain(domain.getAssignee()))
                .reporter(domain.getReporter() == null ? null : UserDto.fromDomain(domain.getReporter()))
                .priority(domain.getPriority())
                .encryptedName(Base64.getEncoder().encodeToString(domain.getEncryptedName()))
                .encryptedContent(domain.getEncryptedContent() == null ? null : Base64.getEncoder().encodeToString(domain.getEncryptedContent()))
                .iv(Base64.getEncoder().encodeToString(domain.getIv()))
                .build();
    }
}
