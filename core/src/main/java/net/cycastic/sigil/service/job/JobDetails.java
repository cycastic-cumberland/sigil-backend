package net.cycastic.sigil.service.job;

import java.util.UUID;

public interface JobDetails {
    UUID getCorrelationId();
    JobType getJobType();
    String getRequestClass();
}
