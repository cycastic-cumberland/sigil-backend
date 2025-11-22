package net.cycastic.sigil.application.pm.task.status.transit.disconnect;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.repository.pm.TaskProgressRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DisconnectTaskStatusCommandHandler extends BaseProjectCommandHandler<DisconnectTaskStatusCommand, Void> {
    private final TaskProgressRepository taskProgressRepository;

    @Override
    protected Void handleInternal(DisconnectTaskStatusCommand command, ProjectPartition projectPartition) {
        taskProgressRepository.deleteByFromStatus_IdAndNextStatus_Id(command.getFromStatusId(), command.getToStatusId());
        return null;
    }
}
