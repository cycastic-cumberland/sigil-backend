package net.cycastic.sigil.application.partition.delete;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import net.cycastic.sigil.application.misc.TransactionalCommand;

@TransactionalCommand
public class DeletePartitionCommand implements Command<Void> {
    public static final DeletePartitionCommand INSTANCE = new DeletePartitionCommand();
}
