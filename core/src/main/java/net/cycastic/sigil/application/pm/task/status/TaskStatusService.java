package net.cycastic.sigil.application.pm.task.status;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.pm.TaskProgressDto;
import net.cycastic.sigil.domain.dto.pm.TaskStatusDto;
import net.cycastic.sigil.domain.model.pm.TaskStatus;
import net.cycastic.sigil.domain.repository.pm.TaskProgressRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskStatusService {
    public record TaskStatusMappingPair(Map<Long, List<TaskProgressDto>> fromToMap, Map<Long, List<TaskProgressDto>> toFromMap){}
    private final TaskProgressRepository taskProgressRepository;

    public TaskStatusMappingPair toMappingPair(Collection<TaskStatus> taskStatuses){
        var progress = taskProgressRepository.findByFromStatusInOrNextStatusIn(taskStatuses, taskStatuses);
        var fromToMap = progress.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getFromStatus().getId(),
                        Collectors.mapping(TaskProgressDto::fromDomain, Collectors.toList())));
        var toFromMap = progress.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getNextStatus().getId(),
                        Collectors.mapping(TaskProgressDto::fromDomain, Collectors.toList())));

        return new TaskStatusMappingPair(fromToMap, toFromMap);
    }

    public Collection<TaskStatusDto> toDto(Collection<TaskStatus> taskStatuses){
        var pair = toMappingPair(taskStatuses);
        var fromToMap = pair.fromToMap();
        var toFromMap = pair.toFromMap();
        var statusDtos = new ArrayList<TaskStatusDto>(taskStatuses.size());
        for (var status : taskStatuses) {
            var dto = TaskStatusDto.fromDomain(status);
            var fromIds = toFromMap.get(dto.getId());
            dto.setPreviousTaskStatuses(fromIds);
            var toIds = fromToMap.get(dto.getId());
            dto.setNextTaskStatuses(toIds);

            statusDtos.add(dto);
        }

        return statusDtos;
    }
}
