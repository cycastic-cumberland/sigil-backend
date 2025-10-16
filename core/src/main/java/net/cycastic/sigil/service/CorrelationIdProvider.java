package net.cycastic.sigil.service;

import jakarta.annotation.Nullable;

import java.util.UUID;

public interface CorrelationIdProvider {
    @Nullable UUID getCorrelationId();
}
