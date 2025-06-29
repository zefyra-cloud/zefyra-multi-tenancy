package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.interceptor;



import com.zefyra.cloud.zefyra_multi_tenancy.enums.TenantEnum;
import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.DataSourceMultiTenantConnectionProvider;
import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util.DatasourceUtils;
import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.sql.DataSource;
import java.util.Map;

import static com.zefyra.cloud.zefyra_multi_tenancy.enums.TenantEnum.TENANT_HEADER;

@Component
public class TenantInterceptor implements HandlerInterceptor {


    @Autowired
    private DataSourceMultiTenantConnectionProvider dataSourceProvider;

    @Autowired
    private DatasourceUtils datasourceUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
            throws Exception {

        String tenantName = request.getHeader(TENANT_HEADER.getValue());

        if (tenantName == null || tenantName.isBlank()) {
            return true;
        }

        if (!dataSourceProvider.containsTenant(tenantName)) {
            Map<String, DataSource> dataSources = datasourceUtils.loadAllTenantDataSources();
            dataSourceProvider.setDataSources(dataSources);

            if (!dataSourceProvider.containsTenant(tenantName)) {
                return true;
            }
        }

        TenantContext.setTenantName(tenantName);
        return true;
    }


    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler,
                           ModelAndView modelAndView) {
        TenantContext.clear();
    }
}
