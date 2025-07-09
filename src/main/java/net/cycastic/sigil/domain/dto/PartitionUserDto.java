package net.cycastic.sigil.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartitionUserDto {
    private String email;
    private String firstName;
    private String lastName;
    private int permissions;
}
