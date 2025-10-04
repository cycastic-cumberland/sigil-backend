package net.cycastic.sigil.application.pm.kanban.query.id;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.domain.dto.pm.KanbanBoardDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.repository.pm.KanbanBoardRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetKanbanBoardByIdCommandHandler extends BaseProjectCommandHandler<GetKanbanBoardByIdCommand, KanbanBoardDto> {
    private final KanbanBoardRepository kanbanBoardRepository;

    @Override
    protected KanbanBoardDto handleInternal(GetKanbanBoardByIdCommand command, ProjectPartition projectPartition) {
        var board = kanbanBoardRepository.findByIdAndProjectPartition_Id(command.getId(), projectPartition.getId())
                .orElseThrow(() -> new RequestException(404, "Board not found"));
        return KanbanBoardDto.fromDomain(board);
    }
}
