package com.zefyra.cloud.zefyra_multi_tenancy.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TenantHeader {
    TENANT_HEADER("X-Tenant-Id");
    private final String value;
}
