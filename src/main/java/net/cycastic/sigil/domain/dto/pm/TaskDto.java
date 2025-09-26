package net.cycastic.sigil.domain.dto.pm;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.domain.model.pm.Task;

import java.util.Base64;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TaskDto extends TaskCardDto {
    private String taskIdentifier;
    private TaskStatusDto taskStatus;
    private UserDto reporter;
    private String encryptedContent;
    // TODO: Label

    public static TaskDto fromDomain(Task domain){
        return fromDomain(domain, TaskDto.builder())
                .taskStatus(domain.getTaskStatus() == null ? null : TaskStatusDto.fromDomain(domain.getTaskStatus()))
                .reporter(domain.getReporter() == null ? null : UserDto.fromDomain(domain.getReporter()))
                .encryptedContent(domain.getEncryptedContent() == null ? null : Base64.getEncoder().encodeToString(domain.getEncryptedContent()))
                .build();
    }
}
