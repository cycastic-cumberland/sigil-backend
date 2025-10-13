package net.cycastic.sigil.application.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvictCacheBackgroundJob {
    private String cacheName;
    private String cacheKey;
}
