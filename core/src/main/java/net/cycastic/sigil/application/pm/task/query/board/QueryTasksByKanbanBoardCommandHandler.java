package net.cycastic.sigil.application.pm.task.query.board;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.domain.dto.pm.TaskCardDto;
import net.cycastic.sigil.domain.dto.pm.TaskCardsDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.repository.pm.KanbanBoardRepository;
import net.cycastic.sigil.domain.repository.pm.TaskRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryTasksByKanbanBoardCommandHandler extends BaseProjectCommandHandler<QueryTasksByKanbanBoardCommand, TaskCardsDto> {
    private final KanbanBoardRepository kanbanBoardRepository;
    private final TaskRepository taskRepository;

    @Override
    protected TaskCardsDto handleInternal(QueryTasksByKanbanBoardCommand command, ProjectPartition projectPartition) {
        var board = kanbanBoardRepository.findByIdAndProjectPartition_Id(command.getKanbanBoardId(), projectPartition.getId())
                .orElseThrow(() -> new RequestException(404, "Board not found"));
        var cards = taskRepository.findByKanbanBoard(board).stream()
                .map(TaskCardDto::fromDomain)
                .toList();
        return new TaskCardsDto(cards);
    }
}
