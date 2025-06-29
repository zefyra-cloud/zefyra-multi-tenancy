package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Getter
@Primary
@Service
@Slf4j
public class DataSourceMultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl<String> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Setter
    private Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    @Override
    public DataSource selectAnyDataSource() {
        if (dataSources.isEmpty()) {
            throw new RuntimeException("No DataSources configured");
        }
        return dataSources.values().iterator().next();
    }

    @Override
    public DataSource selectDataSource(String tenantIdentifier) {
        DataSource ds = dataSources.get(tenantIdentifier);
        if (ds == null) {
            throw new RuntimeException("Unknown tenant " + tenantIdentifier);
        }

        log.info("Using tenant '{}'", tenantIdentifier);
        try (Connection conn = ds.getConnection()) {
            log.info("Connected to DB URL: {}", conn.getMetaData().getURL());
        } catch (SQLException e) {
            log.warn("Could not get connection metadata", e);
        }

        return ds;
    }

    public void addDataSourceIfAbsent(String tenantId, Supplier<DataSource> supplier) {
        dataSources.computeIfAbsent(tenantId, id -> {
            log.info("Creating DataSource for tenant '{}'", id);
            return supplier.get();
        });
        log.info("Ensured DataSource for tenant '{}'", tenantId);
    }

    public boolean containsTenant(String tenantId) {
        return dataSources.containsKey(tenantId);
    }

}
