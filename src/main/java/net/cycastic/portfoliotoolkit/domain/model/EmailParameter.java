package net.cycastic.portfoliotoolkit.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailParameter {
    private String name;
    private EmailParameterType type;
}
