package net.cycastic.sigil.application.partition.member.self;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.domain.dto.PartitionUserDto;

public class GetSelfPartitionUserCommand implements Command<PartitionUserDto> {
    public static final GetSelfPartitionUserCommand INSTANCE = new GetSelfPartitionUserCommand();
}
