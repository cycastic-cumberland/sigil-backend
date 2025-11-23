package net.cycastic.sigil.application.pm.task.status.transit.connect;

import net.cycastic.sigil.application.validation.CommandValidator;
import net.cycastic.sigil.domain.exception.RequestException;
import org.springframework.stereotype.Component;

@Component
public class ConnectTaskStatusCommandValidator implements CommandValidator<SaveTaskStatusProgressionCommand, Void> {
    @Override
    public void validate(SaveTaskStatusProgressionCommand command) {
        for (var connection : command.getConnections()){
            if (connection.getFromStatusId() == connection.getToStatusId()){
                throw new RequestException(400, "From ID must not be To ID");
            }
        }
    }
}
