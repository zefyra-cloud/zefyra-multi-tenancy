package com.zefyra.cloud.zefyra_multi_tenancy.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SystemTenantSchemaEnum {
    SYSTEM_SCHEMA("system"),
    MASTER_SCHEMA("master"),
    KEYCLOAK_SCHEMA("keycloak");

    private final String value;
}
