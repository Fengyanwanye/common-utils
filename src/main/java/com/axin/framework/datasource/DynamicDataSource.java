package com.axin.framework.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 动态数据源路由器
 * 
 * <p>继承Spring的AbstractRoutingDataSource，实现动态数据源切换功能。
 * 根据当前线程上下文中的数据源标识，动态路由到对应的数据源(主库/从库)。</p>
 * 
 * <p>使用场景：读写分离、多数据源切换</p>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/9/23 10:46
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private static final Logger log = LoggerFactory.getLogger(DynamicDataSource.class);

    /**
     * 构造动态数据源
     * 
     * @param defaultTargetDataSource 默认数据源(主库)
     * @param targetDataSources 所有可用的数据源映射表，key为数据源名称，value为数据源对象
     */
    public DynamicDataSource(DataSource defaultTargetDataSource, Map<Object, Object> targetDataSources) {
        // 设置默认数据源
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        // 设置所有数据源映射
        super.setTargetDataSources(targetDataSources);
        // 初始化数据源映射
        super.afterPropertiesSet();
        log.info("动态数据源初始化完成，可用数据源: {}", targetDataSources.keySet());
    }

    /**
     * 确定当前要使用的数据源key
     * 
     * <p>该方法在每次数据库操作时被调用，根据线程上下文返回对应的数据源标识。
     * 如果返回null，则使用默认数据源。</p>
     * 
     * @return 当前线程绑定的数据源标识(MASTER/SLAVE)
     */
    @Override
    protected Object determineCurrentLookupKey() {
        String dataSourceType = DynamicDataSourceContextHolder.getDataSourceType();
        log.debug("当前数据源: {}", dataSourceType);
        return dataSourceType;
    }
}
