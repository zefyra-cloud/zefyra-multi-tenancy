package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy;

import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util.DatasourceUtils;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.net.URL;
import java.sql.Connection;
import java.util.List;

@Component
@Slf4j
public class TenantLiquibaseRunner implements ApplicationRunner {

    @Value("${tenants.liquidbase.enabled:false}")
    private boolean ENABLED;

    @Value("${tenants.liquidbase.changeLogPath:db.changelog/db.changelog-master.yaml}")
    private String CHANGELOG_PATH;

    @Value("${tenants.liquidbase.defaultSchema:public}")
    private String PUBLIC_SCHEMA;

    @Autowired
    private DatasourceUtils datasourceUtils;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!ENABLED) {
            log.info("TenantLiquibaseRunner disabilitato via configuration.");
            return;
        }

        List<DatasourceUtils.TenantInfo> tenants = datasourceUtils.loadAllTenants();

        tenants.stream()
                .filter(t -> !t.tenantId().equals(0L))
                .forEach(tenant -> {
                    DataSource ds = datasourceUtils.createDataSource(tenant);
                    runLiquibase(ds);
                });
    }

    @SneakyThrows
    private void runLiquibase(DataSource dataSource) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(CHANGELOG_PATH);

        if (resource == null) {
            log.info("Liquibase changelog file not found: {}. Skipping migration.", CHANGELOG_PATH);
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            log.info("Running Liquibase on DB: {}", connection.getMetaData().getURL());
            log.info("Username: {}", connection.getMetaData().getUserName());

            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setDefaultSchemaName(PUBLIC_SCHEMA);

            Liquibase liquibase = new Liquibase(
                    CHANGELOG_PATH,
                    new ClassLoaderResourceAccessor(),
                    database
            );
            liquibase.update("");
        }
    }
}

