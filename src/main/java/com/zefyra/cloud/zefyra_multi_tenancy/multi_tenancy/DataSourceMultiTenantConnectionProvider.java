package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy;

import lombok.Setter;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Map;

@Primary
@Service
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
        return ds;
    }

    public boolean containsTenant(String tenantId) {
        return dataSources.containsKey(tenantId);
    }

    public void addDataSource(String tenantId, DataSource dataSource) {
        dataSources.put(tenantId, dataSource);
    }

    public Map<String, DataSource> getDataSources() {
        return dataSources;
    }
}
