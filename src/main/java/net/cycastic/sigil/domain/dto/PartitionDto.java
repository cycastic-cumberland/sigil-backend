package net.cycastic.sigil.domain.dto;

import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.domain.model.listing.Partition;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class PartitionDto {
    private int id;
    private String partitionPath;
    private CipherDto userPartitionKey;
    private boolean serverSideKeyDerivation;
    private List<String> permissions;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static PartitionDto fromDomain(Partition partition){
        return PartitionDto.builder()
                .id(partition.getId())
                .partitionPath(partition.getPartitionPath())
                .serverSideKeyDerivation(partition.getServerPartitionKey() != null)
                .createdAt(partition.getCreatedAt())
                .updatedAt(partition.getUpdatedAt())
                .build();
    }
}
