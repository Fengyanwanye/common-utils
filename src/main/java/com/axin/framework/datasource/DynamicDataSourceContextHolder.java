package com.axin.framework.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 动态数据源上下文持有者
 * 
 * <p>使用ThreadLocal实现线程隔离的数据源标识存储，
 * 保证多线程环境下每个线程使用独立的数据源不会相互干扰。</p>
 * 
 * <p>线程安全：通过ThreadLocal保证线程安全性</p>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/9/23 10:47
 */
public class DynamicDataSourceContextHolder {

    private static final Logger log = LoggerFactory.getLogger(DynamicDataSourceContextHolder.class);

    /**
     * 线程本地变量，存储当前线程使用的数据源类型
     * 每个线程拥有独立的数据源标识，互不影响
     */
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的数据源类型
     * 
     * @param dataSourceType 数据源类型标识(MASTER/SLAVE)
     */
    public static void setDataSourceType(String dataSourceType) {
        log.info("切换到{}数据源", dataSourceType);
        CONTEXT_HOLDER.set(dataSourceType);
    }

    /**
     * 获取当前线程的数据源类型
     * 
     * @return 数据源类型标识，如果未设置则返回null
     */
    public static String getDataSourceType() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前线程的数据源类型
     * 
     * <p>防止内存泄漏，在数据库操作完成后必须调用此方法清理</p>
     */
    public static void clearDataSourceType() {
        CONTEXT_HOLDER.remove();
    }
}
