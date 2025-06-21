package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util;

import lombok.SneakyThrows;
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

    private static final String QUERY = "SELECT tenant_id, url, username, password FROM tenants";

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

        String connectionURL = getJdbcUrl(masterJdbcPrefix, masterUrl, masterPort, masterDatabaseName);

        Connection conn = DriverManager.getConnection(connectionURL, masterUsername, masterPassword);
        PreparedStatement stmt = conn.prepareStatement(QUERY);
        ResultSet rs = stmt.executeQuery();

        List<TenantInfo> tenantInfos = new ArrayList<>();

        while (rs.next()) {
            TenantInfo tenant = extractTenantInfo(rs);
            tenantInfos.add(tenant);
        }

        return tenantInfos;
    }

    private TenantInfo extractTenantInfo(ResultSet rs) throws SQLException {
        return new TenantInfo(
                rs.getString("tenant_id"),
                rs.getString("url"),
                rs.getString("username"),
                rs.getString("password")
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

    private String getJdbcUrl(String masterJdbcPrefix, String masterUrl, String masterPort, String masterDatabaseName) {
        return String.format("%s://%s:%s/%s", masterJdbcPrefix, masterUrl, masterPort, masterDatabaseName);
    }

    public record TenantInfo(String tenantId, String url, String username, String password) {}
}
