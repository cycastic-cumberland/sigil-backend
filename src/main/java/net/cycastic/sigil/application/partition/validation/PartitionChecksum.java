package net.cycastic.sigil.application.partition.validation;


import com.fasterxml.jackson.annotation.JsonIgnore;

public interface PartitionChecksum {
    String getPartitionChecksum();

    @JsonIgnore
    default boolean isMd5() {
        return false;
    }
}
