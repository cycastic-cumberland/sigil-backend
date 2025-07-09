package net.cycastic.sigil.domain.dto;

import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.domain.model.Partition;

import java.time.OffsetDateTime;

@Data
@Builder
public class PartitionDto {
    private int id;
    private String partitionPath;
    private CipherDto userPartitionKey;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static PartitionDto fromDomain(Partition partition){
        return PartitionDto.builder()
                .id(partition.getId())
                .partitionPath(partition.getPartitionPath())
                .createdAt(partition.getCreatedAt())
                .updatedAt(partition.getUpdatedAt())
                .build();
    }
}
