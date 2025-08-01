package net.cycastic.sigil.service.impl;

import net.cycastic.sigil.configuration.limit.LimitConfigurations;
import net.cycastic.sigil.domain.dto.TypedUsageDetailsDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.tenant.Tenant;
import net.cycastic.sigil.domain.model.tenant.UsageType;
import net.cycastic.sigil.service.LimitProvider;
import net.cycastic.sigil.service.UsageDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class TieredLimitProvider implements LimitProvider {
    private final HashMap<UsageType, TypedUsageDetailsDto> tiers;

    @Autowired
    public TieredLimitProvider(LimitConfigurations limitConfigurations){
        tiers = HashMap.newHashMap(limitConfigurations.getLimits().length);
        for (var limit : limitConfigurations.getLimits()){
            if (tiers.containsKey(limit.getUsageType())){
                throw new IllegalStateException("Usage type " + limit.getUsageType().toString() + " was already defined");
            }

            tiers.put(limit.getUsageType(), limit);
        }
    }

    @Override
    public UsageDetails extractUsageDetails(Tenant tenant) {
        var details = tiers.get(tenant.getUsageType());
        if (details == null){
            throw new RequestException(500, "Usage type is undefined: " + tenant.getUsageType());
        }

        return details;
    }
}
