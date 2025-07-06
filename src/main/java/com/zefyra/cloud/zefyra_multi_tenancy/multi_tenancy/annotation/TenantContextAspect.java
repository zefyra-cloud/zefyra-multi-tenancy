package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.annotation;

import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util.TenantContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantContextAspect {
    @Around("@annotation(withTenantContext)")
    public Object switchTenantContext(ProceedingJoinPoint pjp, WithTenantContext withTenantContext) throws Throwable {
        Long originalTenantId = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(withTenantContext.value());
            return pjp.proceed();
        } finally {
            TenantContext.setTenantId(originalTenantId);
        }
    }
}
