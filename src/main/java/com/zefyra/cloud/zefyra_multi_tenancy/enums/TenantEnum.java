package com.zefyra.cloud.zefyra_multi_tenancy.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TenantEnum {
    SYSTEM("system"),
    MASTER("master"),
    KEYCLOAK("keycloak"),
    TENANT_HEADER("TENANT-NAME");

    private final String value;
}
