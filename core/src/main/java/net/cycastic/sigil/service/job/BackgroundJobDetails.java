package net.cycastic.sigil.service.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BackgroundJobDetails implements JobDetails {
    private UUID id;

    private UUID correlationId;

    private OffsetDateTime scheduledAt;

    private String requestClass;

    private String data;

    @Override
    public JobType getJobType() {
        return JobType.DEFERRED;
    }
}
