package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TenantContext {

    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();

    public static void setTenantId(Long tenantName) {
        currentTenant.set(tenantName);
    }

    public static Long getTenantId() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}