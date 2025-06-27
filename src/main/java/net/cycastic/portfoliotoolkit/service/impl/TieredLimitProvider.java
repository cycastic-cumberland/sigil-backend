package net.cycastic.portfoliotoolkit.service.impl;

import net.cycastic.portfoliotoolkit.configuration.limit.LimitConfigurations;
import net.cycastic.portfoliotoolkit.domain.dto.TypedUsageDetailsDto;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.model.UsageType;
import net.cycastic.portfoliotoolkit.domain.model.User;
import net.cycastic.portfoliotoolkit.service.LimitProvider;
import net.cycastic.portfoliotoolkit.service.UsageDetails;
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
