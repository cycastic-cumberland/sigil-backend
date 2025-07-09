package net.cycastic.sigil.application.partition.delete;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;

public class DeletePartitionCommand implements Command<@Null Object> {
    public static final DeletePartitionCommand INSTANCE = new DeletePartitionCommand();
}
