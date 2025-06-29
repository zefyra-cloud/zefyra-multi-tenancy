package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy;


import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util.DatasourceUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TenantDataSourceInitializer {

    @Autowired
    private DataSourceMultiTenantConnectionProvider connectionProvider;

    @Autowired
    private DatasourceUtils datasourceUtils;

    @PostConstruct
    public void loadTenants() {
        Map<String, DataSource> baseDataSources = datasourceUtils.loadSystemDefaultTenantsDataSources();

        Map<String, DataSourceMultiTenantConnectionProvider.TimedDataSource> timedDataSources =
                baseDataSources.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> new DataSourceMultiTenantConnectionProvider.TimedDataSource(e.getValue())
                        ));

        connectionProvider.setDataSources(timedDataSources);

        log.info("Loaded default system tenants: {}", timedDataSources.keySet());
    }

}
