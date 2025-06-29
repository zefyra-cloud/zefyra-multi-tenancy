package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Getter
@Primary
@Service
@Slf4j
public class DataSourceMultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl<String> {

    private static final long serialVersionUID = 1L;

    @Setter
    private Map<String, DataSource> dataSources;

    @Override
    public DataSource selectAnyDataSource() {
        if (dataSources == null || dataSources.isEmpty()) {
            throw new RuntimeException("No DataSources configured");
        }
        return dataSources.values().iterator().next();
    }

    @Override
    public DataSource selectDataSource(String tenantIdentifier) {
        if (dataSources == null) {
            throw new RuntimeException("No DataSources configured");
        }
        DataSource ds = dataSources.get(tenantIdentifier);
        if (ds == null) {
            throw new RuntimeException("Unknown tenant " + tenantIdentifier);
        }

        // Logga info sul DB selezionato
        log.info("Using tenant '{}'", tenantIdentifier);
        try (Connection conn = ds.getConnection()) {
            log.info("Connected to DB URL: {}", conn.getMetaData().getURL());
        } catch (SQLException e) {
            log.warn("Could not get connection metadata", e);
        }

        return ds;
    }

    public boolean containsTenant(String tenantId) {
        return dataSources.containsKey(tenantId);
    }

}
