package net.cycastic.sigil.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PartitionUserDto {
    private String email;
    private String firstName;
    private String lastName;
    private List<String> permissions;
}
