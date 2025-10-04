package net.cycastic.sigil.controller.pm;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.kanban.query.id.GetKanbanBoardByIdCommand;
import net.cycastic.sigil.application.pm.kanban.query.project.QueryKanbanBoardByProjectCommand;
import net.cycastic.sigil.application.pm.kanban.save.SaveKanbanBoardCommand;
import net.cycastic.sigil.controller.annotation.RequirePartitionId;
import net.cycastic.sigil.controller.annotation.RequireTenantId;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.dto.pm.KanbanBoardDto;
import net.cycastic.sigil.domain.exception.RequestException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequireTenantId
@RequirePartitionId
@RequiredArgsConstructor
@RequestMapping("api/pm/kanban")
public class KanbanController {
    private final Pipelinr pipelinr;

    @PostMapping
    public IdDto saveBoard(@Valid @RequestBody SaveKanbanBoardCommand command){
        return pipelinr.send(command);
    }

    @GetMapping
    public PageResponseDto<KanbanBoardDto> queryBoards(@Valid QueryKanbanBoardByProjectCommand command){
        return pipelinr.send(command);
    }

    @GetMapping("id/{id}")
    public KanbanBoardDto getBoardById(@PathVariable String id){
        return pipelinr.send(new GetKanbanBoardByIdCommand(ApplicationUtilities.tryParseInt(id)
                .orElseThrow(() -> new RequestException(400, "ID is not an integer"))));
    }
}
