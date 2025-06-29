package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy;

import com.zaxxer.hikari.HikariDataSource;
import com.zefyra.cloud.zefyra_multi_tenancy.config.TenantEvictionProperties;
import com.zefyra.cloud.zefyra_multi_tenancy.enums.TenantEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
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

    @Setter
    private Map<String, TimedDataSource> dataSources = new ConcurrentHashMap<>();

    @Autowired
    private TenantEvictionProperties evictionProperties;

    @Override
    public DataSource selectAnyDataSource() {
        if (dataSources.isEmpty()) {
            throw new RuntimeException("No DataSources configured");
        }
        return dataSources.values().iterator().next().getDataSource();
    }

    @Override
    public DataSource selectDataSource(String tenantIdentifier) {
        TimedDataSource timed = dataSources.get(tenantIdentifier);
        if (timed == null) {
            throw new RuntimeException("Unknown tenant " + tenantIdentifier);
        }

        DataSource ds = timed.getDataSource();
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
            return new TimedDataSource(supplier.get());
        });
        log.info("Ensured DataSource for tenant '{}'", tenantId);
    }

    public boolean containsTenant(String tenantId) {
        return dataSources.containsKey(tenantId);
    }

    @Scheduled(fixedDelayString = "${tenants.eviction.cleanupDelayMs}")
    public void cleanupUnusedDataSources() {
        long cutoff = System.currentTimeMillis() - evictionProperties.getMaxIdleMinutes() * 60 * 1000;
        log.info("Running cleanup of unused DataSources...");

        dataSources.entrySet().removeIf(entry -> {
            String tenantId = entry.getKey();
            TimedDataSource ds = entry.getValue();

            if (isProtectedTenant(tenantId)) return false;

            if (ds.shouldRemove(cutoff)) {
                log.info("Closing and removing unused DataSource for tenant '{}'", tenantId);
                ds.close();
                return true;
            }
            return false;
        });
    }

    private boolean isProtectedTenant(String tenantId) {
        return tenantId.equalsIgnoreCase(TenantEnum.MASTER.getValue()) ||
                tenantId.equalsIgnoreCase(TenantEnum.SYSTEM.getValue()) ||
                tenantId.equalsIgnoreCase(TenantEnum.KEYCLOAK.getValue());
    }

    public static class TimedDataSource {
        private final DataSource dataSource;
        @Getter
        private volatile long lastAccess;
        private volatile boolean markedForRemoval = false;

        public TimedDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            this.lastAccess = System.currentTimeMillis();
        }

        public DataSource getDataSource() {
            this.lastAccess = System.currentTimeMillis();
            this.markedForRemoval = false;
            return dataSource;
        }

        public boolean shouldRemove(long cutoff) {
            if (lastAccess < cutoff) {
                if (markedForRemoval) {
                    log.info("DataSource for tenant is marked for removal and last accessed at {} ms ago", System.currentTimeMillis() - lastAccess);
                    return true; // confermato: possiamo chiudere
                } else {
                    log.info("DataSource for tenant last accessed at {} ms ago, marking for removal", System.currentTimeMillis() - lastAccess);
                    markedForRemoval = true;
                }
            }
            return false;
        }

        public void close() {
            if (dataSource instanceof HikariDataSource hikari) {
                hikari.close();
            }
        }
    }
}
