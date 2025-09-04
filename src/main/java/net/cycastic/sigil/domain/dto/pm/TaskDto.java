package net.cycastic.sigil.domain.dto.pm;

import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.domain.model.pm.Task;
import net.cycastic.sigil.domain.model.pm.TaskPriority;

@Data
@Builder
public class TaskDto {
    private int id;
    private String taskIdentifier;
    private TaskStatusDto taskStatus;
    private UserDto assignee;
    private UserDto reporter;
    private TaskPriority taskPriority;
    private String encryptedName;
    private String encryptedContent;
    private String iv;
    // TODO: Label

    public static TaskDto fromDomain(Task task){
        throw new RuntimeException("Not implemented");
    }
}
