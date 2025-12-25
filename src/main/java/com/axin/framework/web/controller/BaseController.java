package com.axin.framework.web.controller;

import com.axin.common.utils.DateUtils;
import com.axin.common.utils.StringUtils;
import com.axin.common.utils.sql.SqlUtil;
import com.axin.framework.web.page.PageDomain;
import com.axin.framework.web.page.TableDataInfo;
import com.axin.framework.web.page.TableSupport;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.List;

/**
 * 控制器基类
 * 
 * <p>提供控制器层的通用功能，所有控制器均应继承此类。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>自动处理日期类型参数的绑定</li>
 *   <li>分页查询的开启和数据返回</li>
 *   <li>提供日志记录器</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * {@code @RestController}
 * {@code @RequestMapping("/user")}
 * public class UserController extends BaseController {
 *     
 *     {@code @GetMapping("/list")}
 *     public ResponseEntity list() {
 *         startPage();  // 开启分页
 *         List{@code <User>} list = userService.list();
 *         return getDataTable(list);  // 返回分页数据
 *     }
 * }
 * </pre>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/19 16:20
 */
public class BaseController {

    /**
     * 日志记录器
     * 
     * <p>子类可直接使用logger进行日志输出</p>
     */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public BaseController() {
    }

    /**
     * 初始化数据绑定器
     * 
     * <p>自动将请求参数中的字符串转换为Date类型。</p>
     * <p>支持多种日期格式的自动识别和解析。</p>
     * 
     * @param binder Web数据绑定器
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    /**
     * 开启分页功能
     * 
     * <p>从请求参数中获取分页信息，并启动PageHelper分页。</p>
     * <p>同时支持GET和POST请求（POST请求需要Content-Type为application/json）。</p>
     * <p>支持的请求参数：</p>
     * <ul>
     *   <li>pageNum: 页码</li>
     *   <li>pageSize: 每页条数</li>
     *   <li>orderByColumn: 排序字段</li>
     *   <li>isAsc: 是否升序</li>
     * </ul>
     * 
     * <p>使用示例（POST请求）：</p>
     * <pre>
     * // 请求体：
     * {
     *   "pageNum": 1,
     *   "pageSize": 10,
     *   "orderByColumn": "createTime",
     *   "isAsc": "desc"
     * }
     * </pre>
     */
    protected void startPage() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        if (StringUtils.isNotNull(pageNum) && StringUtils.isNotNull(pageSize)) {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.startPage(pageNum, pageSize, orderBy);
        }
    }

    /**
     * 返回分页数据
     * 
     * <p>将查询结果列表封装为TableDataInfo对象，包含总数和数据列表。</p>
     * 
     * @param list 查询结果列表
     * @return 包含分页信息的ResponseEntity对象
     */
    protected <T> ResponseEntity<TableDataInfo<List<T>>> getDataTable(List<T> list) {
        TableDataInfo<List<T>> rspData = new TableDataInfo<>();
        rspData.setRows(list);
        rspData.setTotal(new PageInfo<>(list).getTotal());
        return ResponseEntity.ok(rspData);
    }

    /**
     * 返回分页数据（支持自定义总数统计）
     * 
     * <p>用于响应数据和总数统计来自不同查询的场景。</p>
     * 
     * @param responseList 响应数据列表
     * @param totalList 用于统计总数的列表
     * @return 包含分页信息的ResponseEntity对象
     */
    protected <T> ResponseEntity<TableDataInfo<List<T>>> getDataTable(List<T> responseList, List<T> totalList) {
        TableDataInfo<List<T>> rspData = new TableDataInfo<>();
        rspData.setRows(responseList);
        rspData.setTotal(new PageInfo<>(totalList).getTotal());
        return ResponseEntity.ok(rspData);
    }
}
