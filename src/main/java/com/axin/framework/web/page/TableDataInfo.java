package com.axin.framework.web.page;

import java.io.Serializable;

/**
 * 分页响应数据封装类
 * 
 * <p>用于封装分页查询的响应数据，包含总记录数和数据列表。</p>
 * 
 * <p>通常用于返回给前端的分页查询结果。</p>
 * 
 * <p>响应数据结构：</p>
 * <pre>
 * {
 *   "total": 100,
 *   "rows": [...]
 * }
 * </pre>
 * 
 * @param <T> 数据列表的类型，通常为List<?>
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/19 16:36
 */
public class TableDataInfo<T> implements Serializable {
    private static final long serialVersionUID = -7385279941556399800L;

    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 数据列表
     */
    private T rows;

    /**
     * 无参构造函数
     */
    public TableDataInfo() {}

    /**
     * 带参数的构造函数
     * 
     * @param rows 数据列表
     * @param total 总记录数
     */
    public TableDataInfo(T rows, int total) {
        this.rows = rows;
        this.total = (long) total;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public T getRows() {
        return rows;
    }

    public void setRows(T rows) {
        this.rows = rows;
    }
}
