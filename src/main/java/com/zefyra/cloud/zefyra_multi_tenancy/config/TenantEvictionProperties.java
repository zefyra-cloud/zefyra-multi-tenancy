package com.zefyra.cloud.zefyra_multi_tenancy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tenants.eviction")
public class TenantEvictionProperties {
    private long maxIdleMinutes = 30; // default di sicurezza
}
