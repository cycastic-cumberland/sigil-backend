package net.cycastic.sigil.application.pm.save;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.listing.PartitionType;
import net.cycastic.sigil.domain.model.pm.KanbanBoard;

import net.cycastic.sigil.domain.repository.pm.KanbanBoardRepository;
import net.cycastic.sigil.domain.repository.pm.ProjectPartitionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveKanbanBoardCommandHandler implements Command.Handler<SaveKanbanBoardCommand, Void> {
    private final PartitionService partitionService;
    private final KanbanBoardRepository kanbanBoardRepository;
    private final ProjectPartitionRepository projectPartitionRepository;

    @Override
    public Void handle(SaveKanbanBoardCommand command) {
        var partition = partitionService.getPartition();
        if (!partition.getPartitionType().equals(PartitionType.PROJECT)){
            throw new RequestException(400, "Is not a project partition");
        }

        KanbanBoard kanbanBoard;
        if (command.getId() != null){
            kanbanBoard = kanbanBoardRepository.findByIdAndProjectPartition_Id(command.getId(), partition.getId())
                    .orElseThrow(() -> new RequestException(404, "Kanban board not found"));
        } else {
            var projectPartition = projectPartitionRepository.findById(partition.getId())
                    .orElseThrow();
            kanbanBoard = new KanbanBoard();
            kanbanBoard.setProjectPartition(projectPartition);
        }

        kanbanBoard.setBoardName(command.getBoardName());
        kanbanBoardRepository.save(kanbanBoard);
        return null;
    }
}
