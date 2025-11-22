package net.cycastic.sigil.application.pm;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.repository.pm.ProjectPartitionRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseProjectCommandHandler<C extends Command<R>, R> implements Command.Handler<C, R> {
    @Autowired
    private ProjectPartitionRepository projectPartitionRepository;

    @Autowired
    private LoggedUserAccessor loggedUserAccessor;

    protected abstract R handleInternal(C command, ProjectPartition projectPartition);

    @Override
    public R handle(C command){
        var partition = loggedUserAccessor.tryGetPartitionId()
                .stream()
                .boxed()
                .flatMap(id -> projectPartitionRepository.findById(id).stream())
                .findFirst()
                .orElseThrow(RequestException::forbidden);
        return handleInternal(command, partition);
    }
}
