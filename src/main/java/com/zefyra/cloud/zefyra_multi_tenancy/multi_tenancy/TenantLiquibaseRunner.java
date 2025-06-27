package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util.DatasourceUtils;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

@Component
@Slf4j
public class TenantLiquibaseRunner implements ApplicationRunner {

    @Autowired
    private DatasourceUtils datasourceUtils;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<DatasourceUtils.TenantInfo> tenants = datasourceUtils.loadAllTenants();

        tenants.stream()
                .filter(t -> !t.tenantId().equalsIgnoreCase("master") && !t.tenantId().equalsIgnoreCase("system"))
                .forEach(tenant -> {
                    DataSource dataSource = createDataSourceForTenant(tenant);
                    runLiquibase(dataSource);
                });
    }

    private DataSource createDataSourceForTenant(DatasourceUtils.TenantInfo tenant) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(tenant.url());
        config.setUsername(tenant.username());
        config.setPassword(tenant.password());
        config.setDriverClassName("org.postgresql.Driver");
        return new HikariDataSource(config);
    }

    @SneakyThrows
    private void runLiquibase(DataSource dataSource) {
        ClassLoader classLoader = getClass().getClassLoader();
        String changelogPath = "db/changelog/db.changelog-master.yaml";

        if (classLoader.getResource(changelogPath) == null) {
            log.info("Liquibase changelog file not found: {}. Skipping migration.", changelogPath);
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            log.info("Running Liquibase on DB: {}", connection.getMetaData().getURL());
            log.info("Username: {}", connection.getMetaData().getUserName());

            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setDefaultSchemaName("public");

            Liquibase liquibase = new Liquibase(
                    changelogPath,
                    new ClassLoaderResourceAccessor(),
                    database
            );
            liquibase.update("");
        }
    }
}

