package com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.annotation;

import com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.util.TenantContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // oppure un valore tipo -100
public class TenantContextAspect {
    @Around("@annotation(com.zefyra.cloud.zefyra_multi_tenancy.multi_tenancy.annotation.WithTenantContext)")
    public Object switchTenantContext(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        WithTenantContext withTenantContext = signature.getMethod().getAnnotation(WithTenantContext.class);

        Long originalTenantId = TenantContext.getTenantId();
        try {
            assert withTenantContext != null;
            TenantContext.setTenantId(withTenantContext.value());
            return pjp.proceed();
        } finally {
            TenantContext.setTenantId(originalTenantId);
        }
    }
}
