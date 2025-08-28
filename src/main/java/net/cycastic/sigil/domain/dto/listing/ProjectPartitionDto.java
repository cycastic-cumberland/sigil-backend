package net.cycastic.sigil.domain.dto.listing;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.model.listing.Partition;

import java.util.Objects;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ProjectPartitionDto extends PartitionDto {
    private String uniqueIdentifier;

    private int latestSprintNumber;

    private int latestTaskId;

    public static ProjectPartitionDto fromDomain(Partition partition){
        var projectPartition = Objects.requireNonNull(partition.getProjectPartition());
        return fromDomain(partition, ProjectPartitionDto.builder())
                .uniqueIdentifier(projectPartition.getUniqueIdentifier())
                .latestSprintNumber(projectPartition.getLatestSprintNumber())
                .latestTaskId(projectPartition.getLatestTaskId())
                .build();
    }
}
