package net.cycastic.sigil.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderItemDto {
    private String name;

    private OffsetDateTime modifiedAt;

    private FolderItemType type;

    private Boolean attachmentUploadCompleted;
}
