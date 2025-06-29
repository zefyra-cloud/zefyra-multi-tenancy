package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy;


import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util.DatasourceUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

@Component
public class TenantDataSourceInitializer {

    @Autowired
    private DataSourceMultiTenantConnectionProvider connectionProvider;

    @Autowired
    private DatasourceUtils datasourceUtils;

    @PostConstruct
    public void loadTenants() {
        Map<String, DataSource> dataSources = datasourceUtils.loadSystemDefaultTenantsDataSources();
        connectionProvider.setDataSources(dataSources);
    }

}
