package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util;

import com.zefyra.cloud.zefyra_multi_tenancy.repositories.TenantRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatasourceUtils {

    @Value("${tenants.master.jdbcPrefix}")
    private String masterJdbcPrefix;

    @Value("${tenants.master.url}")
    private String masterUrl;

    @Value("${tenants.master.port}")
    private String masterPort;

    @Value("${tenants.master.databaseName}")
    private String masterDatabaseName;

    @Value("${tenants.master.username}")
    private String masterUsername;

    @Value("${tenants.master.password}")
    private String masterPassword;

    @Value("${tenants.master.schema}")
    private String masterSchema;

    private static final String MASTER_SCHEMA = "master";
    private static final String SYSTEM_SCHEMA = "system";

    private static final String COLUMN_TENANT_ID = "tenant_id";
    private static final String COLUMN_URL = "host_url";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    private static final String TABLE_TENANTS = "tenants";

    private static final String QUERY = String.format(
            "SELECT %s, %s, %s, %s FROM %s",
            COLUMN_TENANT_ID, COLUMN_URL, COLUMN_USERNAME, COLUMN_PASSWORD, TABLE_TENANTS
    );

    @Autowired
    private TenantRepository tenantRepository;

    @SneakyThrows
    public Map<String, DataSource> loadAllTenantDataSources() {
        Map<String, DataSource> dataSources = new HashMap<>();
        loadAllTenants().forEach(tenant ->
                dataSources.put(tenant.tenantId(), createDataSource(tenant.url(), tenant.username(), tenant.password()))
        );
        return dataSources;
    }

    @SneakyThrows
    public List<TenantInfo> loadAllTenants() throws SQLException {

        List<TenantInfo> tenantInfos = new ArrayList<>(createDataSourceForZefyraDB());

        TenantContext.setTenantName(SYSTEM_SCHEMA);

        tenantRepository.findAll().forEach(entity -> {
            tenantInfos.add(new TenantInfo(
                    entity.getTenantId(),
                    entity.getHostUrl(),
                    entity.getUsername(),
                    entity.getPassword()
            ));
        });

        return tenantInfos;
    }

    private List<TenantInfo> createDataSourceForZefyraDB() {
        return List.of(
                new TenantInfo(SYSTEM_SCHEMA, getJdbcUrl(masterJdbcPrefix, masterUrl, masterPort, masterDatabaseName, SYSTEM_SCHEMA), masterUsername, masterPassword),
                new TenantInfo(MASTER_SCHEMA, getJdbcUrl(masterJdbcPrefix, masterUrl, masterPort, masterDatabaseName, MASTER_SCHEMA), masterUsername, masterPassword)
        );
    }

    private DataSource createDataSource(String url, String username, String password) {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    private String getJdbcUrl(String jdbcPrefix, String host, String port, String dbName, String schema) {
        return String.format("%s://%s:%s/%s?currentSchema=%s", jdbcPrefix, host, port, dbName, schema);
    }

    public record TenantInfo(String tenantId, String url, String username, String password) {}
}
