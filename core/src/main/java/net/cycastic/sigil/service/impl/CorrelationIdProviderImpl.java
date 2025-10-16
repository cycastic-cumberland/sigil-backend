package net.cycastic.sigil.service.impl;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.SessionStorage;
import net.cycastic.sigil.service.CorrelationIdProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CorrelationIdProviderImpl implements CorrelationIdProvider {
    private static final Logger logger = LoggerFactory.getLogger(CorrelationIdProviderImpl.class);
    private final SessionStorage sessionStorage;
    private final ThreadLocal<UUID> idOverride = new ThreadLocal<>();

    private UUID getSessionCorrelationId(){
        try {
            return sessionStorage.getCorrelationId();
        } catch (Exception e){
            logger.warn("Failed to get a UUID from local session", e);
            return null;
        }
    }

    @Override
    public @Nullable UUID getCorrelationId() {
        var tl = idOverride.get();
        if (tl == null){
            return getSessionCorrelationId();
        }

        return tl;
    }

    public void setIdOverride(@Nullable UUID idOverride){
        this.idOverride.set(idOverride);
    }
}
