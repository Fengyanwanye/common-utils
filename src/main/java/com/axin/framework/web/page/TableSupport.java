package com.axin.framework.web.page;

import com.axin.common.utils.ServletUtils;

/**
 * 分页参数解析工具类
 * 
 * <p>从当前请求中解析分页相关参数，并构建分页对象。</p>
 * 
 * <p>支持的请求参数：</p>
 * <ul>
 *   <li>pageNum: 页码</li>
 *   <li>pageSize: 每页条数</li>
 *   <li>orderByColumn: 排序字段</li>
 *   <li>isAsc: 排序方向</li>
 * </ul>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/19 16:47
 */
public class TableSupport {
    /**
     * 页码参数名
     */
    public static final String PAGE_NUM = "pageNum";
    
    /**
     * 每页条数参数名
     */
    public static final String PAGE_SIZE = "pageSize";
    
    /**
     * 排序字段参数名
     */
    public static final String ORDER_BY_COLUMN = "orderByColumn";
    
    /**
     * 排序方向参数名
     */
    public static final String IS_ASC = "isAsc";

    public TableSupport() {
    }

    /**
     * 获取分页参数对象
     * 
     * <p>从当前请求中提取分页相关的参数。</p>
     * 
     * @return 分页参数对象
     */
    public static PageDomain getPageDomain() {
        PageDomain pageDomain = new PageDomain();
        pageDomain.setPageNum(ServletUtils.getParameterToInt("pageNum"));
        pageDomain.setPageSize(ServletUtils.getParameterToInt("pageSize"));
        pageDomain.setOrderByColumn(ServletUtils.getParameter("orderByColumn"));
        pageDomain.setIsAsc(ServletUtils.getParameter("isAsc"));
        return pageDomain;
    }

    /**
     * 构建分页请求对象
     * 
     * <p>与getPageDomain()功能相同，提供更语义化的方法名。</p>
     * 
     * @return 分页参数对象
     */
    public static PageDomain buildPageRequest() {
        return getPageDomain();
    }
}
