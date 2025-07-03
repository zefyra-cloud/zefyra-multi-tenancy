package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy;

import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Service;

@Service
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<Long> {

    @Override
    public Long resolveCurrentTenantIdentifier() {
        Long currentTenantName = TenantContext.getTenantName();
        long DEFAULT_TENANT_NAME = 0L;
        return (currentTenantName != null) ? currentTenantName : DEFAULT_TENANT_NAME;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
