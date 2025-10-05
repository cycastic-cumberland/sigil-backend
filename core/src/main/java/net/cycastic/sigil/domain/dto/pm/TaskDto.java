package net.cycastic.sigil.domain.dto.pm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;
import net.cycastic.sigil.domain.model.pm.Task;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaskDto extends TaskCardDto {
    private int projectPartitionId;
    private String taskIdentifier;
    private TaskStatusDto taskStatus;
    private UserDto reporter;
    private CipherDto encryptedContent;
    // TODO: Label

    public static TaskDto fromDomain(Task domain, int projectPartitionId){
        return fromDomain(domain, TaskDto.builder())
                .projectPartitionId(projectPartitionId)
                .taskStatus(domain.getTaskStatus() == null ? null : TaskStatusDto.fromDomain(domain.getTaskStatus()))
                .reporter(domain.getReporter() == null ? null : UserDto.fromDomain(domain.getReporter()))
                .encryptedContent(domain.getEncryptedContent() == null ? null : CipherDto.fromDomain(domain.getEncryptedContent()))
                .build();
    }
}
