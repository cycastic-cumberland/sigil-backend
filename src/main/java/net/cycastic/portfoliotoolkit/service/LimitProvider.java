package net.cycastic.portfoliotoolkit.service;

import net.cycastic.portfoliotoolkit.domain.model.User;

public interface LimitProvider {
    UsageDetails extractUsageDetails(User user);
}
