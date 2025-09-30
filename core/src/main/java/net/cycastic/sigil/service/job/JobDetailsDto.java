package net.cycastic.sigil.service.job;

import lombok.Data;

@Data
public class JobDetailsDto implements JobDetails{
    private JobType jobType;
}
