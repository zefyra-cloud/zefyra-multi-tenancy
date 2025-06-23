package com.zefyra.cloud.zefyra_multi_tenancy.repositories;

import com.zefyra.cloud.zefyra_multi_tenancy.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {
}
