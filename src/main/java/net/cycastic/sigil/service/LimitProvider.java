package net.cycastic.sigil.service;

import net.cycastic.sigil.domain.model.User;

public interface LimitProvider {
    UsageDetails extractUsageDetails(User user);
}
