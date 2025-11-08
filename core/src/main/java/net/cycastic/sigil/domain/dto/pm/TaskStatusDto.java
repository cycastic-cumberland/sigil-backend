package net.cycastic.sigil.domain.dto.pm;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.model.pm.TaskStatus;
import net.cycastic.sigil.domain.model.pm.TaskUniqueStereotype;

import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusDto {
    private long id;
    private String statusName;

    @Nullable
    private Collection<TaskProgressDto> previousTaskStatuses;

    @Nullable
    private Collection<TaskProgressDto> nextTaskStatuses;

    @Nullable
    private TaskUniqueStereotype stereotype;

    public static TaskStatusDto fromDomain(TaskStatus taskStatus){
        return TaskStatusDto.builder()
                .id(taskStatus.getId())
                .statusName(taskStatus.getStatusName())
                .stereotype(taskStatus.getTaskUniqueStatus() == null ? null : taskStatus.getTaskUniqueStatus().getTaskUniqueStereotype())
                .build();
    }
}
