package net.cycastic.sigil.domain.dto.pm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;
import net.cycastic.sigil.domain.model.pm.TaskComment;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private long id;
    private UserDto sender;
    private String taskIdentifier;
    private CipherDto encryptedContent;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static CommentDto fromDomain(TaskComment domain){
        return CommentDto.builder()
                .id(domain.getId())
                .sender(UserDto.fromDomain(domain.getSender()))
                .taskIdentifier(domain.getTask().getTaskIdentifier())
                .encryptedContent(CipherDto.fromDomain(domain.getEncryptedContent()))
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
