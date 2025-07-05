package net.cycastic.portfoliotoolkit.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JwksDto {
    private List<JwkDto> keys;
}
