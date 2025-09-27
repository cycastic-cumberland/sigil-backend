package net.cycastic.sigil.service;

import net.cycastic.sigil.domain.model.tenant.Tenant;

public interface LimitProvider {
    UsageDetails extractUsageDetails(Tenant user);
}
