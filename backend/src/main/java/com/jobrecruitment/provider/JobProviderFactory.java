package com.jobrecruitment.provider;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JobProviderFactory {

    private final List<JobSourceProvider> providers;

    public JobProviderFactory(List<JobSourceProvider> providers) {
        this.providers = providers;
    }

    public Optional<JobSourceProvider> getProvider(String providerName) {
        if (providerName == null) return Optional.empty();
        return providers.stream()
                .filter(p -> p.supports(providerName.trim()))
                .findFirst();
    }
}
