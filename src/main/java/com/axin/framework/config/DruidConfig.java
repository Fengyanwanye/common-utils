package com.axin.framework.config;

import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceBuilder;
import com.alibaba.druid.spring.boot3.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.util.Utils;
import com.axin.framework.aspectj.lang.enums.DataSourceType;
import com.axin.framework.datasource.DynamicDataSource;
import jakarta.servlet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Druid数据源配置类
 * 
 * <p>配置动态数据源，支持主从数据库读写分离。</p>
 * 
 * <p>功能特性：</p>
 * <ul>
 *   <li>支持主从数据源配置</li>
 *   <li>从数据源可选配置</li>
 *   <li>支持数据库密码加密</li>
 *   <li>集成Druid监控功能</li>
 * </ul>
 * 
 * <p>配置示例：</p>
 * <pre>
 * spring:
 *   datasource:
 *     druid:
 *       enabled: true
 *       master:
 *         url: jdbc:mysql://localhost:3306/master_db
 *         username: root
 *         password: password
 *       slave:
 *         enabled: true
 *         url: jdbc:mysql://localhost:3306/slave_db
 * </pre>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/9/23 10:54
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.datasource.druid", name = "enabled", havingValue = "true", matchIfMissing = false)
@AutoConfigureBefore({DruidDataSourceAutoConfigure.class, DataSourceAutoConfiguration.class})
public class DruidConfig {

    /**
     * 数据库连接信息是否加密的标识
     * 默认为false，如果为true则使用AxinDruidDataSourceWrapper进行解密
     */
    @Value("${encryJdbc.flag:false}")
    private Boolean encryJdbc;

    /**
     * Spring应用上下文，用于获取Bean
     */
    @Autowired
    private ApplicationContext applicationContext;

    public DruidConfig() {
    }

    /**
     * 创建数据源的通用方法
     * 
     * @param properties 数据源属性配置
     * @param type 数据源类型
     * @return 数据源实例
     */
    protected static <T> T createDataSource(DataSourceProperties properties, Class<? extends DataSource> type) {
        return (T)properties.initializeDataSourceBuilder().type(type).build();
    }

    /**
     * 创建主数据源Bean
     * 
     * <p>主数据源用于处理写操作和强一致性读操作。</p>
     * <p>如果配置了encryJdbc.flag=true，则使用AxinDruidDataSourceWrapper支持密码解密。</p>
     * 
     * @return 主数据源实例
     */
    @Bean
    @ConfigurationProperties("spring.datasource.druid.master")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.master", name = "url")
    public DataSource masterDataSource() {
        return this.encryJdbc ? new AxinDruidDataSourceWrapper() : DruidDataSourceBuilder.create().build();
    }

    /**
     * 创建从数据源Bean（可选）
     * 
     * <p>从数据源用于处理查询操作，分担主库压力。</p>
     * <p>只有当配置了spring.datasource.druid.slave.enabled=true时才会创建。</p>
     * 
     * @return 从数据源实例
     */
    @Bean("slaveDataSource")
    @ConfigurationProperties("spring.datasource.druid.slave")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.slave", name = "enabled", havingValue = "true")
    public DataSource slaveDataSource() {
        return this.encryJdbc ? new AxinDruidDataSourceWrapper() : DruidDataSourceBuilder.create().build();
    }

    /**
     * 创建动态数据源Bean
     * 
     * <p>动态数据源是整个数据源切换机制的核心，负责根据上下文动态路由到不同的数据源。</p>
     * 
     * <p>@Primary注解确保该数据源作为默认数据源被注入到其他组件中。</p>
     * 
     * @param masterDataSource 主数据源（必选）
     * @return 动态数据源实例
     */
    @Bean("dynamicDataSource")
    @Primary  // 标记为主要数据源，优先注入
    @ConditionalOnProperty(prefix = "spring.datasource.druid.master", name = "url")
    public DynamicDataSource dynamicDataSource(DataSource masterDataSource) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        // 添加主数据源到映射表
        targetDataSources.put(DataSourceType.MASTER.name(), masterDataSource);

        // 尝试获取从数据源（可选配置）
        try {
            DataSource slaveDataSource = applicationContext.getBean("slaveDataSource", DataSource.class);
            targetDataSources.put(DataSourceType.SLAVE.name(), slaveDataSource);
            System.out.println("成功加载数据源: SLAVE");
        } catch (Exception e) {
            System.out.println("未配置从数据源，仅使用主数据源");
        }

        // 创建动态数据源，主数据源作为默认数据源
        return new DynamicDataSource(masterDataSource, targetDataSources);
    }

    /**
     * 配置Druid监控页面过滤器
     * 
     * <p>用于去除Druid监控页面底部的广告信息，提供更简洁的监控界面。</p>
     * <p>只有当配置了spring.datasource.druid.statViewServlet.enabled=true时才会生效。</p>
     * 
     * @param properties Druid统计属性配置
     * @return 过滤器注册Bean
     */
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.druid.statViewServlet.enabled", havingValue = "true")
    public FilterRegistrationBean removeDruidFilterRegistrationBean(DruidStatProperties properties) {
        DruidStatProperties.StatViewServlet config = properties.getStatViewServlet();

        // 获取Druid监控页面的URL模式
        String pattern = (config.getUrlPattern() != null) ? config.getUrlPattern() : "/druid/*";
        String commonJsPattern = pattern.replaceAll("\\*", "js/common.js");
        String filePattern = "support/http/resource/js/common.js";

        // 创建过滤器，用于处理common.js文件，去除广告信息
        Filter filter = new Filter() {

            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
                // 过滤器初始化方法
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                chain.doFilter(request, response);

                // 重置响应缓冲区
                response.resetBuffer();

                // 读取common.js文件内容并去除广告相关的HTML标签
                String text = Utils.readFromResource("support/http/resource/js/common.js");
                text.replaceAll("<a.*?banner\"></a></br>", "");
                text.replaceAll("powered.*?shrek.wang</a>", "");
                response.getWriter().write(text);
            }

            @Override
            public void destroy() {
                // 过滤器销毁方法
            }
        };
        
        // 注册过滤器
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns(new String[] {commonJsPattern});
        return registrationBean;
    }
}
