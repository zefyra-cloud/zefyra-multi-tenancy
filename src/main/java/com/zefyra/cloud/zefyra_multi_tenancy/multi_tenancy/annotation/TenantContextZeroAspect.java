package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.annotation;

import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util.TenantContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantContextZeroAspect {

    @Around("@annotation(WithTenantZeroContext)")
    public Object switchTenantContextToZero(ProceedingJoinPoint pjp) throws Throwable {
        TenantContext.setTenantId(0L);
        return pjp.proceed();
    }
}
