package net.cycastic.sigil.domain.dto.pm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusesDto {
    private List<TaskStatusDto> taskStatuses;
}
