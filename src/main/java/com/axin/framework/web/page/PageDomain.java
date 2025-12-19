package com.axin.framework.web.page;

import com.axin.common.utils.StringUtils;

/**
 * 分页参数对象
 * 
 * <p>封装分页查询的相关参数。</p>
 * 
 * <p>包含参数：</p>
 * <ul>
 *   <li>pageNum: 页码，从1开始</li>
 *   <li>pageSize: 每页显示条数</li>
 *   <li>orderByColumn: 排序字段（驼峰命名）</li>
 *   <li>isAsc: 排序方向（"asc"或"desc"）</li>
 * </ul>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/19 16:34
 */
public class PageDomain {
    /**
     * 页码，从1开始
     */
    protected Integer pageNum;
    
    /**
     * 每页显示条数
     */
    protected Integer pageSize;
    
    /**
     * 排序字段名（驼峰命名，如"userName"）
     */
    protected String orderByColumn;
    
    /**
     * 排序方向（"asc"或"desc"）
     */
    protected String isAsc;

    public PageDomain() {
    }

    /**
     * 获取ORDER BY子句
     * 
     * <p>将驼峰命名的字段名转换为下划线命名，并拼接排序方向。</p>
     * 
     * <p>示例：</p>
     * <pre>
     * orderByColumn = "userName"
     * isAsc = "asc"
     * 返回: "user_name asc"
     * </pre>
     * 
     * @return 格式化后的ORDER BY子句，如果没有排序字段则返回空字符串
     */
    public String getOrderBy() {
        return StringUtils.isEmpty(orderByColumn) ? "" : StringUtils.toUnderScoreCase(orderByColumn) + " " + isAsc;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrderByColumn() {
        return orderByColumn;
    }

    public void setOrderByColumn(String orderByColumn) {
        this.orderByColumn = orderByColumn;
    }

    public String getIsAsc() {
        return isAsc;
    }

    public void setIsAsc(String isAsc) {
        this.isAsc = isAsc;
    }
}
