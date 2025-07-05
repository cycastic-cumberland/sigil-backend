package net.cycastic.sigil.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailParameterDto {
    private String name;
    private EmailParameterType type;
    private String value;
}
