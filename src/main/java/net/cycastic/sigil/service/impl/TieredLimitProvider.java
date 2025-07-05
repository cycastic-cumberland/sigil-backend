package net.cycastic.sigil.service.impl;

import net.cycastic.sigil.configuration.limit.LimitConfigurations;
import net.cycastic.sigil.domain.dto.TypedUsageDetailsDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.UsageType;
import net.cycastic.sigil.domain.model.User;
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
    public UsageDetails extractUsageDetails(User user) {
        var details = tiers.get(user.getUsageType());
        if (details == null){
            throw new RequestException(500, "Usage type is undefined: " + user.getUsageType());
        }

        return details;
    }
}
