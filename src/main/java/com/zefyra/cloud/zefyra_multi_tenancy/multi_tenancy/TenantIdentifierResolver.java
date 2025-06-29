package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy;

import com.zefyra.cloud.zefyra_multi_tenancy.enums.TenantEnum;
import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Service;

@Service
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String currentTenantName = TenantContext.getTenantName();
        String DEFAULT_TENANT_NAME = TenantEnum.SYSTEM.getValue();
        return (currentTenantName != null) ? currentTenantName : DEFAULT_TENANT_NAME;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
