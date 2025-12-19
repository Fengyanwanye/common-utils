package com.axin.common.utils.sql;

import com.axin.common.exception.BaseException;
import com.axin.common.utils.StringUtils;

/**
 * SQL工具类
 * 
 * <p>提供SQL安全相关的工具方法，主要用于防止SQL注入攻击。</p>
 * 
 * <p>特性：</p>
 * <ul>
 *   <li>验证ORDER BY子句的合法性</li>
 *   <li>防止通过排序字段进行SQL注入</li>
 *   <li>支持标准SQL排序字段字符验证</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>分页查询时验证排序字段</li>
 *   <li>动态SQL拼接时的安全检查</li>
 * </ul>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/19 16:57
 */
public class SqlUtil {

    /**
     * SQL排序字段的合法字符正则表达式
     * 
     * <p>允许的字符：字母、数字、下划线、空格、逗号、点</p>
     * <p>用于防止SQL注入攻击</p>
     */
    public static String SQL_PATTERN = "[a-zA-Z0-9_\\ \\,\\.]+";

    public SqlUtil() {
    }

    /**
     * 验证并转义ORDER BY排序字段
     * 
     * <p>检查排序字段是否包含非法字符，防止SQL注入攻击。</p>
     * 
     * <p>使用示例：</p>
     * <pre>
     * String orderBy = SqlUtil.escapeOrderBySql("user_name ASC, create_time DESC");
     * </pre>
     * 
     * @param value 排序字段字符串，如"user_name ASC"或"id DESC, name ASC"
     * @return 验证通过后的排序字段字符串
     * @throws BaseException 如果排序字段包含非法字符
     */
    public static String escapeOrderBySql(String value) {
        if (StringUtils.isNotEmpty(value) && !isValidOrderBySql(value)) {
            throw new BaseException("参数不符合规范，不能进行查询");
        } else {
            return value;
        }
    }

    /**
     * 验证ORDER BY排序字段是否合法
     * 
     * <p>检查字符串是否只包含字母、数字、下划线、空格、逗号和点。</p>
     * 
     * @param value 待验证的排序字段字符串
     * @return 如果字符串合法返回true，否则返回false
     */
    public static boolean isValidOrderBySql(String value) {
        return value.matches(SQL_PATTERN);
    }
}
