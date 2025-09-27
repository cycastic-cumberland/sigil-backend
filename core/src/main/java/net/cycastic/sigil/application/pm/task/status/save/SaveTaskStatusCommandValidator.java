package net.cycastic.sigil.application.pm.task.status.save;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.validation.CommandValidator;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SaveTaskStatusCommandValidator implements CommandValidator<SaveTaskStatusCommand, IdDto> {
    @Override
    public void validate(SaveTaskStatusCommand command) {
        if (command.getId() == null && command.getKanbanBoardId() == null){
            throw new ValidationException(Map.of("kanbanBoardId", List.of("must not be empty")));
        }
    }
}
