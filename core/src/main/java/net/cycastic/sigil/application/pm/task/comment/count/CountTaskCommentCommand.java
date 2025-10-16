package net.cycastic.sigil.application.pm.task.comment.count;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.domain.dto.CountDto;

@Data
public class CountTaskCommentCommand implements Command<CountDto> {
    @NotEmpty
    private String taskId;
}
