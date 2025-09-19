package net.cycastic.sigil.domain.dto.listing;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;
import net.cycastic.sigil.domain.model.listing.Partition;
import net.cycastic.sigil.domain.model.listing.PartitionType;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@SuperBuilder
public class PartitionDto {
    private int id;
    private String partitionPath;
    private CipherDto userPartitionKey;
    private boolean serverSideKeyDerivation;
    private List<String> permissions;
    private PartitionType partitionType;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    protected static <C extends PartitionDto, B extends PartitionDto.PartitionDtoBuilder<C, B>> B fromDomain(Partition partition, PartitionDto.PartitionDtoBuilder<C, B> builder){
        return builder.id(partition.getId())
                .partitionPath(partition.getPartitionPath())
                .serverSideKeyDerivation(partition.getServerPartitionKey() != null)
                .partitionType(partition.getPartitionType())
                .createdAt(partition.getCreatedAt())
                .updatedAt(partition.getUpdatedAt());
    }

    public static PartitionDto fromDomain(Partition partition){
        return fromDomain(partition, PartitionDto.builder()).build();
    }
}
