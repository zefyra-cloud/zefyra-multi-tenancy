package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setTenantName(String tenantName) {
        currentTenant.set(tenantName);
    }

    public static String getTenantName() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}