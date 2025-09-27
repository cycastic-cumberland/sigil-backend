package net.cycastic.sigil.domain.dto.pm;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

@Data
@AllArgsConstructor
public class TaskStatusesDto {
    private Collection<TaskStatusDto> taskStatuses;
}
