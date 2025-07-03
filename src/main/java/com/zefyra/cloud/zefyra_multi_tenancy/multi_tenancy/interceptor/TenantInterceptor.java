package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.interceptor;


import com.zefyra.cloud.zefyra_multi_tenancy.enums.TenantHeader;
import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.DataSourceMultiTenantConnectionProvider;
import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util.DatasourceUtils;
import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import static com.zefyra.cloud.zefyra_multi_tenancy.enums.TenantHeader.TENANT_HEADER;

@Component
public class TenantInterceptor implements HandlerInterceptor {


    @Autowired
    private DataSourceMultiTenantConnectionProvider dataSourceProvider;

    @Autowired
    private DatasourceUtils datasourceUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {

        String tenantIdHeader = request.getHeader(TENANT_HEADER.getValue());

        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            return true;
        }

        Long tenantId = Long.parseLong(tenantIdHeader);

        dataSourceProvider.addDataSourceIfAbsent(
                tenantId,
                () -> datasourceUtils.loadTenant(tenantId).getValue()
        );

        TenantContext.setTenantId(tenantId);
        return true;
    }


    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler,
                           ModelAndView modelAndView) {
        TenantContext.clear();
    }
}
