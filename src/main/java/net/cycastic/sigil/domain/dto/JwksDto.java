package net.cycastic.sigil.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JwksDto {
    private List<JwkDto> keys;
}
