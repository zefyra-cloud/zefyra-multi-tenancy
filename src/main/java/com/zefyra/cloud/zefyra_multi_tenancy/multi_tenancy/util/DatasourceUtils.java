package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zefyra.cloud.zefyra_multi_tenancy.enums.SystemTenantSchemaEnum.*;

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

    private static final String COLUMN_TENANT_ID = "tenant_id";
    private static final String COLUMN_URL = "host_url";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    private static final String TABLE_TENANTS = "tenants";

    private static final String QUERY = String.format(
            "SELECT %s, %s, %s, %s FROM %s",
            COLUMN_TENANT_ID, COLUMN_URL, COLUMN_USERNAME, COLUMN_PASSWORD, TABLE_TENANTS
    );

    @SneakyThrows
    public List<TenantInfo> loadAllTenants() throws SQLException {
        String connectionURL = getJdbcUrl(masterJdbcPrefix, masterUrl, masterPort, masterDatabaseName, masterSchema);

        List<TenantInfo> tenantInfos = new ArrayList<>(createDataSourceForZefyraDB());

        try (
                Connection conn = DriverManager.getConnection(connectionURL, masterUsername, masterPassword);
                PreparedStatement stmt = conn.prepareStatement(QUERY);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                tenantInfos.add(extractTenantInfo(rs));
            }
        }

        return tenantInfos;
    }

    @SneakyThrows
    public Map<Long, DataSource> loadSystemDefaultTenantsDataSources() {
        Map<Long, DataSource> dataSources = new HashMap<>();
        loadSystemDefaultTenants().forEach(tenant ->
                dataSources.put(tenant.tenantId(), createDataSource(tenant))
        );
        return dataSources;
    }

    @SneakyThrows
    public List<TenantInfo> loadSystemDefaultTenants() throws SQLException {
        return new ArrayList<>(createDataSourceForZefyraDB());
    }

    @SneakyThrows
    public Map.Entry<Long, DataSource> loadTenant(Long tenantId) {
        String connectionURL = getJdbcUrl(masterJdbcPrefix, masterUrl, masterPort, masterDatabaseName, masterSchema);
        String singleQuery = QUERY + " WHERE " + COLUMN_TENANT_ID + " = ?";

        try (
                Connection conn = DriverManager.getConnection(connectionURL, masterUsername, masterPassword);
                PreparedStatement stmt = conn.prepareStatement(singleQuery)
        ) {
            stmt.setLong(1, tenantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    TenantInfo info = extractTenantInfo(rs);
                    DataSource ds = createDataSource(info);
                    return Map.entry(info.tenantId(), ds);
                } else {
                    throw new IllegalArgumentException("Tenant not found: " + tenantId);
                }
            }
        }
    }

    private List<TenantInfo> createDataSourceForZefyraDB() {
        return List.of(
                new TenantInfo(0L, getJdbcUrl(masterJdbcPrefix, masterUrl, masterPort, masterDatabaseName, SYSTEM_SCHEMA.getValue()), masterUsername, masterPassword),
                new TenantInfo(1L, getJdbcUrl(masterJdbcPrefix, masterUrl, masterPort, masterDatabaseName, MASTER_SCHEMA.getValue()), masterUsername, masterPassword),
                new TenantInfo(2L, getJdbcUrl(masterJdbcPrefix, masterUrl, masterPort, masterDatabaseName, KEYCLOAK_SCHEMA.getValue()), masterUsername, masterPassword)
        );
    }

    private TenantInfo extractTenantInfo(ResultSet rs) throws SQLException {
        return new TenantInfo(
                rs.getLong(COLUMN_TENANT_ID),
                rs.getString(COLUMN_URL),
                rs.getString(COLUMN_USERNAME),
                rs.getString(COLUMN_PASSWORD)
        );
    }

    public DataSource createDataSource(TenantInfo tenantInfo) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(tenantInfo.url);
        config.setUsername(tenantInfo.username);
        config.setPassword(tenantInfo.password);
        config.setDriverClassName("org.postgresql.Driver");

        // 🔑 LIMITA IL POOL
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setIdleTimeout(30_000); // 30 secondi

        config.setPoolName("Tenant-" + tenantInfo.tenantId());

        return new HikariDataSource(config);
    }

    private String getJdbcUrl(String jdbcPrefix, String host, String port, String dbName, String schema) {
        return String.format("%s://%s:%s/%s?currentSchema=%s", jdbcPrefix, host, port, dbName, schema);
    }

    public record TenantInfo(Long tenantId, String url, String username, String password) {}
}
