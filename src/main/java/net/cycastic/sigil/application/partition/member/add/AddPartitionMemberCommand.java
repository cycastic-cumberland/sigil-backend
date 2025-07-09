package net.cycastic.sigil.application.partition.member.add;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class AddPartitionMemberCommand implements Command<@Null Object> {
    private String email;
    private String wrappedPartitionUserKey;
    private int permissions;
}
