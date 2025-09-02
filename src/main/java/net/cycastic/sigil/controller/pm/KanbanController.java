package net.cycastic.sigil.controller.pm;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.save.SaveKanbanBoardCommand;
import net.cycastic.sigil.controller.annotation.RequirePartitionId;
import net.cycastic.sigil.controller.annotation.RequireTenantId;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireTenantId
@RequirePartitionId
@RequiredArgsConstructor
@RequestMapping("api/pm/kanban")
public class KanbanController {
    private final Pipelinr pipelinr;

    @PostMapping
    public void saveBoard(@Valid @RequestBody SaveKanbanBoardCommand command){
        pipelinr.send(command);
    }
}
