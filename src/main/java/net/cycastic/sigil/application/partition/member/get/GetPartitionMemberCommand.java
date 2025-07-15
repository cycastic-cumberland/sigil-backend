package net.cycastic.sigil.application.partition.member.get;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.domain.dto.PartitionUserDto;

@Data
public class GetPartitionMemberCommand implements Command<PartitionUserDto> {
    private String email;
}
