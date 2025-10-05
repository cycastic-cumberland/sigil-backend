package net.cycastic.sigil.configuration.cache;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfigurations implements CachingConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(CacheConfigurations.class);
    public static final String CACHE_MANAGER_BEAN_NAME = "cacheManager";

    public static class Presets {
        public static final String SHORT_LIVE_CACHE = "ShortTtl";
        public static final String LONG_LIVE_CACHE = "LongTtl";
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(@NonNull RuntimeException exception,
                                            @NonNull Cache cache,
                                            @NonNull Object key) {
                logger.warn("Failed to get value of key {} from cache {}", key, cache.getName(), exception);
            }

            @Override
            public void handleCachePutError(@NonNull RuntimeException exception,
                                            @NonNull Cache cache,
                                            @NonNull Object key,
                                            Object value) {
                logger.warn("Failed to put value of key {} from cache {}", key, cache.getName(), exception);
            }

            @Override
            public void handleCacheEvictError(@NonNull RuntimeException exception,
                                              @NonNull Cache cache,
                                              @NonNull Object key) {
                logger.warn("Failed to evict value of key {} from cache {}", key, cache.getName(), exception);
            }

            @Override
            public void handleCacheClearError(@NonNull RuntimeException exception,
                                              @NonNull Cache cache) {
                logger.error("Failed to clear cache {}", cache.getName(), exception);
            }
        };
    }

    @Bean
    public CacheManager cacheManager(Environment environment,
                                     RedisConnectionFactory redisConnectionFactory,
                                     RedisSerializer<?> redisSerializer) {
        var host = environment.getProperty("spring.data.redis.host");
        if (host == null){
            return new NoOpCacheManager();
        }

        var config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer));
        var cacheConfigs = Map.of(Presets.SHORT_LIVE_CACHE, config.entryTtl(Duration.ofSeconds(30)),
                Presets.LONG_LIVE_CACHE, config.entryTtl(Duration.ofDays(3)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config.entryTtl(Duration.ofMinutes(10)))
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
