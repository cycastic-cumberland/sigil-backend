package net.cycastic.sigil.domain.dto.pm;

import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.domain.model.pm.TaskStatus;

@Data
@Builder
public class TaskStatusDto {
    private long id;
    private String statusName;
    private Long previousTaskStatusId;
    private Long nextTaskStatusId;

    public static TaskStatusDto fromDomain(TaskStatus taskStatus){
        return TaskStatusDto.builder()
                .id(taskStatus.getId())
                .statusName(taskStatus.getStatusName())
                .build();
    }
}
