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
import java.net.URL;
import java.sql.Connection;
import java.util.List;

import static com.zefyra.cloud.zefyra_multi_tenancy.enums.TenantEnum.*;

@Component
@Slf4j
public class TenantLiquibaseRunner implements ApplicationRunner {

    @Autowired
    private DatasourceUtils datasourceUtils;

    private static final String PUBLIC_SCHEMA = "public";
    private static final String CHANGELOG_PATH = "db.changelog/db.changelog-master.yaml";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<DatasourceUtils.TenantInfo> tenants = datasourceUtils.loadAllTenants();

        tenants.stream()
                .filter(t -> {
                    String id = t.tenantId();
                    return !id.equalsIgnoreCase(MASTER.getValue())
                            && !id.equalsIgnoreCase(SYSTEM.getValue())
                            && !id.equalsIgnoreCase(KEYCLOAK.getValue());
                })
                .forEach(tenant -> {
                    DataSource ds = createDataSourceForTenant(tenant);
                    runLiquibase(ds);
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

