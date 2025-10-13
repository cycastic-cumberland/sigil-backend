package net.cycastic.sigil.application.cache;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.service.job.BackgroundJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EvictCacheBackgroundJobHandler implements BackgroundJobHandler<EvictCacheBackgroundJob> {
    private static final Logger logger = LoggerFactory.getLogger(EvictCacheBackgroundJobHandler.class);
    private final CacheManager cacheManager;

    @Override
    public void process(EvictCacheBackgroundJob data) {
        var cache = cacheManager.getCache(data.getCacheName());
        if (cache == null){
            logger.warn("Cache not found: {}", data.getCacheName());
            return;
        }

        cache.evictIfPresent(data.getCacheKey());
    }
}
