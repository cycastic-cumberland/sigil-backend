package net.cycastic.sigil.application.pm.task.status.transit.connect;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Connection {
    @Min(1)
    private long fromStatusId;

    @Min(1)
    private long toStatusId;

    @NotBlank
    private String statusName;
}
