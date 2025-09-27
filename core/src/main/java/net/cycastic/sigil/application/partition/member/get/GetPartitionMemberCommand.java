package net.cycastic.sigil.application.partition.member.get;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.domain.dto.PartitionUserDto;

@Data
public class GetPartitionMemberCommand implements Command<PartitionUserDto> {
    @NotNull
    @Email
    private String email;
}
