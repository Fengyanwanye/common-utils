package com.axin.framework.aspectj;

import com.axin.common.utils.StringUtils;
import com.axin.framework.aspectj.lang.annotation.DataSource;
import com.axin.framework.datasource.DynamicDataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 数据源切换AOP切面
 * 
 * <p>拦截带有@DataSource注解的方法或类，在方法执行前切换数据源，
 * 执行后恢复默认数据源，实现动态数据源切换功能。</p>
 * 
 * <p>执行顺序：@Order(1) 确保在事务切面之前执行</p>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/9/23 13:23
 */
@Aspect
@Order(1)  // 优先级为1，确保在@Transactional之前执行
@Component
public class DataSourceAspect {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 定义切点：拦截所有使用@DataSource注解的方法或类
     * 
     * <p>支持两种使用方式：</p>
     * <ul>
     *   <li>方法级别：@DataSource标注在方法上</li>
     *   <li>类级别：@DataSource标注在类上，作用于类的所有方法</li>
     * </ul>
     */
    @Pointcut("@annotation(com.axin.framework.aspectj.lang.annotation.DataSource) || @within(com.axin.framework.aspectj.lang.annotation.DataSource)")
    public void dsPointCut() {
    }

    /**
     * 环绕通知：在目标方法执行前后进行数据源切换
     * 
     * <p>执行流程：</p>
     * <ol>
     *   <li>解析@DataSource注解，获取目标数据源</li>
     *   <li>设置线程上下文中的数据源标识</li>
     *   <li>执行目标方法</li>
     *   <li>清理线程上下文，防止内存泄漏</li>
     * </ol>
     * 
     * @param point 切点信息，包含目标方法的相关信息
     * @return 目标方法的返回值
     * @throws Throwable 目标方法可能抛出的异常
     */
    @Around("dsPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        DataSource dataSource = getDataSource(point);

        if (StringUtils.isNotNull(dataSource)) {
            logger.info("========== 切换到 {} 数据源 ==========", dataSource.value().name());
            DynamicDataSourceContextHolder.setDataSourceType(dataSource.value().name());
        } else {
            logger.warn("未找到@DataSource注解，使用默认数据源");
        }

        try {
            // 执行目标方法
            return point.proceed();
        } finally {
            // 方法执行完毕后，清理数据源上下文，避免ThreadLocal内存泄漏
            DynamicDataSourceContextHolder.clearDataSourceType();
            logger.debug("清理数据源上下文");
        }
    }

    /**
     * 获取目标方法或类上的@DataSource注解
     * 
     * <p>查找优先级：方法注解 > 类注解</p>
     * <p>如果方法上有注解，优先使用方法上的；否则使用类上的注解</p>
     * 
     * @param point 切点信息
     * @return DataSource注解对象，如果都没有则返回null
     */
    public DataSource getDataSource(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature)point.getSignature();
        // 优先查找方法上的注解
        DataSource dataSource = (DataSource)AnnotationUtils.findAnnotation(signature.getMethod(), DataSource.class);

        if (Objects.nonNull(dataSource)) {
            return dataSource;
        }

        // 方法上没有，则查找类上的注解
        return (DataSource)AnnotationUtils.findAnnotation(signature.getDeclaringType(), DataSource.class);
    }
}
