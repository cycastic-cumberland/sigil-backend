package net.cycastic.sigil.domain.dto.pm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.model.pm.TaskProgress;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskProgressDto {
    private long fromStatusId;

    private long toStatusId;

    private String name;

    public static TaskProgressDto fromDomain(TaskProgress domain){
        return TaskProgressDto.builder()
                .fromStatusId(domain.getFromStatus().getId())
                .toStatusId(domain.getNextStatus().getId())
                .name(domain.getProgressionName())
                .build();
    }
}
