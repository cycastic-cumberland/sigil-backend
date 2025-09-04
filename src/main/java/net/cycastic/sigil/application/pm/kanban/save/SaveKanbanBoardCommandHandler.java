package net.cycastic.sigil.application.pm.kanban.save;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.KanbanBoard;

import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.repository.pm.KanbanBoardRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveKanbanBoardCommandHandler extends BaseProjectCommandHandler<SaveKanbanBoardCommand, IdDto> {
    private final KanbanBoardRepository kanbanBoardRepository;

    @Override
    protected IdDto handleInternal(SaveKanbanBoardCommand command, ProjectPartition projectPartition) {
        KanbanBoard kanbanBoard;
        if (command.getId() != null){
            kanbanBoard = kanbanBoardRepository.findByIdAndProjectPartition_Id(command.getId(), projectPartition.getId())
                    .orElseThrow(() -> new RequestException(404, "Board not found"));
        } else {
            kanbanBoard = new KanbanBoard();
            kanbanBoard.setProjectPartition(projectPartition);
        }

        kanbanBoard.setBoardName(command.getBoardName());
        kanbanBoardRepository.save(kanbanBoard);
        return new IdDto(kanbanBoard.getId());
    }
}
