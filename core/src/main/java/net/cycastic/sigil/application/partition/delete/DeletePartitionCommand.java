package net.cycastic.sigil.application.partition.delete;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;

@TransactionalCommand
public class DeletePartitionCommand implements Command<Void> {
    public static final DeletePartitionCommand INSTANCE = new DeletePartitionCommand();
}
