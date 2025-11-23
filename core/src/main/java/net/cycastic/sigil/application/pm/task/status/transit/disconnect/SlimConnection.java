package net.cycastic.sigil.application.pm.task.status.transit.disconnect;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlimConnection {
    @Min(1)
    private long fromStatusId;

    @Min(1)
    private long toStatusId;
}
