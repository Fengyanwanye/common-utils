package com.axin.framework.aspectj.lang.annotation;


import com.axin.framework.aspectj.lang.enums.DataSourceType;

import java.lang.annotation.*;

/**
 * 数据源切换注解
 * 
 * <p>用于标注需要使用特定数据源的方法或类。</p>
 * 
 * <p>使用示例：</p>
 * <pre>
 * // 方法级别使用从库
 * {@code @DataSource(DataSourceType.SLAVE)}
 * public List&lt;User&gt; queryUsers() {
 *     return userMapper.selectList(null);
 * }
 * 
 * // 类级别使用从库
 * {@code @DataSource(DataSourceType.SLAVE)}
 * public class UserQueryService {
 *     // 该类所有方法都使用从库
 * }
 * </pre>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/9/23 13:20
 */
@Target({ElementType.METHOD, ElementType.TYPE})  // 可以标注在方法或类上
@Retention(RetentionPolicy.RUNTIME)  // 运行时保留，可通过反射获取
@Documented  // 包含在JavaDoc中
@Inherited  // 子类可继承该注解
public @interface DataSource {

    /**
     * 指定要使用的数据源类型
     * 
     * @return 数据源类型，默认使用主库(MASTER)
     */
    DataSourceType value() default DataSourceType.MASTER;
}
