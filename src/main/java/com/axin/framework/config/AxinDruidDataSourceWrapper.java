package com.axin.framework.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.axin.framework.utils.DatasourceUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

/**
 * Druid数据源包装器
 * 
 * <p>继承DruidDataSource，实现数据库连接信息（用户名、密码）的自动解密功能。</p>
 * 
 * <p>使用场景：当配置文件中的数据库密码是加密存储时，该类会在数据源初始化时自动解密。</p>
 * 
 * <p>配置示例：</p>
 * <pre>
 * encryJdbc.flag=true  # 启用密码解密
 * spring.datasource.druid.master.username=加密后的用户名
 * spring.datasource.druid.master.password=加密后的密码
 * </pre>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/9/23 10:07
 */
@ConfigurationProperties("spring.datasource.druid")
public class AxinDruidDataSourceWrapper extends DruidDataSource implements InitializingBean {

    /**
     * Spring Boot数据源基础属性配置
     */
    @Autowired
    private DataSourceProperties basicProperties;

    public AxinDruidDataSourceWrapper() {
    }

    /**
     * Bean属性设置完成后的回调方法
     * 
     * <p>在数据源初始化时自动解密用户名和密码，确保使用明文连接数据库。</p>
     * 
     * <p>处理流程：</p>
     * <ol>
     *   <li>如果未设置用户名/密码，从basicProperties中获取</li>
     *   <li>如果用户名/密码不为空，调用解密工具进行解密</li>
     *   <li>设置URL和驱动类名</li>
     * </ol>
     * 
     * @throws Exception 解密失败或属性设置失败时抛出异常
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 处理用户名
        if (super.getUsername() == null) {
            super.setUsername(this.basicProperties.determineUsername());
        }

        // 解密用户名
        if (Objects.nonNull(super.getUsername())) {
            try {
                super.setUsername(DatasourceUtils.decrypt(super.getUsername()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 处理密码
        if (super.getPassword() == null) {
            super.setPassword(this.basicProperties.determinePassword());
        }

        // 解密密码
        if (Objects.nonNull(super.getPassword())) {
            try {
                super.setPassword(DatasourceUtils.decrypt(super.getPassword()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 设置数据库URL
        if (getUrl() == null) {
            super.setUrl(this.basicProperties.determineUrl());
        }

        // 设置数据库驱动类名
        if (super.getDriverClassName() == null) {
            super.setDriverClassName(this.basicProperties.determineDriverClassName());
        }
    }
}
