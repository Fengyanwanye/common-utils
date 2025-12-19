package com.axin.framework.aspectj.lang.enums;

/**
 * 数据源类型枚举
 * 
 * <p>定义系统支持的数据源类型，用于主从数据库读写分离场景。</p>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/9/23 12:29
 */
public enum DataSourceType {

    /**
     * 主库数据源
     * 用于写操作(INSERT、UPDATE、DELETE)和强一致性读操作
     */
    MASTER,

    /**
     * 从库数据源
     * 用于读操作(SELECT)，分担主库压力，提高查询性能
     */
    SLAVE;
}
