package net.cycastic.sigil.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailParametersDto {
    private EmailParameterDto[] parameters;
}
