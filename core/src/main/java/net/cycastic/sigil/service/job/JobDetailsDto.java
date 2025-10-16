package net.cycastic.sigil.service.job;

import lombok.Data;

import java.util.UUID;

@Data
public class JobDetailsDto implements JobDetails{
    private UUID correlationId;
    private JobType jobType;
    private String requestClass;
}
