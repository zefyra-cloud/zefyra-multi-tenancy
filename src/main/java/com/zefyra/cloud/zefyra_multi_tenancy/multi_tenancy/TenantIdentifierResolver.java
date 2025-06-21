package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy;

import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String currentTenantName = TenantContext.getTenantName();
        String DEFAULT_TENANT_NAME = "tenant1";
        return (currentTenantName != null) ? currentTenantName : DEFAULT_TENANT_NAME;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
