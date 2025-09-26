package net.cycastic.sigil.application.partition.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProjectPartitionPayload {
    @NotBlank
    @Pattern(
            regexp = "^[A-Z]+[0-9]*$",
            message = "Project identifier may only have uppercase characters and numbers"
    )
    @Size(min = 2, max = 16, message = "Identifier must be 2-16 characters long")
    private String uniqueIdentifier;
}
