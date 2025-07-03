package com.zefyra.cloud.zefyra_multi_tenancy.config;


import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.DataSourceMultiTenantConnectionProvider;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties({ JpaProperties.class })
@EnableTransactionManagement
public class MultiTenantJPAConfiguration {

    @Autowired
    private JpaProperties jpaProperties;

    @Autowired
    private DataSourceMultiTenantConnectionProvider dataSourceMultiTenantConnectionProvider;

    @Autowired
    private MultiTenantConnectionProvider<String> connectionProvider;

    @Bean(name = "multipleDataSources")
    public Map<Long, DataSource> repositoryDataSources() {
        return dataSourceMultiTenantConnectionProvider.getDataSources().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getDataSource()
                ));
    }

    @Bean
    public EntityManagerFactory entityManagerFactory(LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return entityManagerFactoryBean.getObject();
    }

    @Bean
    public PlatformTransactionManager multiTenantTxManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
            MultiTenantConnectionProvider<Long> multiTenantConnectionProvider,
            CurrentTenantIdentifierResolver<Long> currentTenantIdentifierResolver) {

        Map<String, Object> hibernateProperties = new LinkedHashMap<>(this.jpaProperties.getProperties());
        hibernateProperties.put("hibernate.multiTenancy", "DATABASE");
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);

        // 👇 Aggiungi queste due proprietà fondamentali
        hibernateProperties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        hibernateProperties.put("hibernate.temp.use_jdbc_metadata_defaults", false);

        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setPackagesToScan(
                "com.zefyra.cloud",
                "com.cloud.zefyra"
        );
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setJpaPropertyMap(hibernateProperties);
        return entityManagerFactoryBean;
    }

}
