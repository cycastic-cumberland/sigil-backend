package net.cycastic.sigil.application.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.service.job.BackgroundJob;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvictCacheBackgroundJob implements BackgroundJob {
    private String cacheName;
    private String cacheKey;
}
