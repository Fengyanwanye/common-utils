package com.axin.common.core.text;

import com.axin.common.utils.StringUtils;

/**
 * 字符串格式化工具类
 * 
 * <p>提供类似SLF4J的占位符字符串格式化功能，使用{}作为占位符。</p>
 * 
 * <p>使用示例：</p>
 * <pre>
 * String result = StrFormatter.format("用户{}登录成功，IP:{}", "张三", "192.168.1.1");
 * // 输出：用户张三登录成功，IP:192.168.1.1
 * </pre>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/17 9:27
 */
public class StrFormatter {

    /**
     * 空JSON字符串常量
     */
    public static final String EMPTY_JSON = "{}";
    
    /**
     * 反斜杠字符
     */
    public static final char C_BACKSLASH = '\\';
    
    /**
     * 占位符起始字符
     */
    public static final char C_DELIM_START = '{';
    
    /**
     * 占位符结束字符
     */
    public static final char C_DELIM_END = '}';

    public StrFormatter() {
    }

    /**
     * 格式化字符串
     * 
     * <p>将字符串中的{}占位符替换为对应的参数值。</p>
     * 
     * <p>特殊处理：</p>
     * <ul>
     *   <li>\{} 会被转义为 {}</li>
     *   <li>\\{} 会被转义为 \{参数值}</li>
     * </ul>
     * 
     * @param strPattern 带有{}占位符的模板字符串
     * @param argArray 替换占位符的参数数组
     * @return 格式化后的字符串
     */
    public static String format(String strPattern, Object... argArray) {
        if (!StringUtils.isEmpty(strPattern) && !StringUtils.isEmpty(argArray)) {
            int strPatternLength = strPattern.length();
            // 创建StringBuilder，预估长度为原字符串长度+50
            StringBuilder sbuf = new StringBuilder(strPatternLength + 50);
            int handledPosition = 0;  // 已处理到的位置

            // 遍历所有参数
            for(int argIndex = 0; argIndex < argArray.length; ++argIndex) {
                // 查找下一个占位符{}
                int delimIndex = strPattern.indexOf("{}", handledPosition);
                if (delimIndex == -1) {
                    // 没有找到更多占位符
                    if (handledPosition == 0) {
                        // 一个占位符都没找到，直接返回原字符串
                        return strPattern;
                    }

                    // 添加剩余的字符串
                    sbuf.append(strPattern, handledPosition, strPatternLength);
                    return sbuf.toString();
                }

                // 处理转义字符
                if (delimIndex > 0 && strPattern.charAt(delimIndex - 1) == '\\') {
                    if (delimIndex > 1 && strPattern.charAt(delimIndex - 2) == '\\') {
                        // \\{} 的情况，保留一个\并替换{}
                        sbuf.append(strPattern, handledPosition, delimIndex - 1);
                        sbuf.append(Convert.utf8Str(argArray[argIndex]));
                        handledPosition = delimIndex + 2;
                    } else {
                        // \{} 的情况，转义为{}
                        --argIndex;  // 参数索引不增加
                        sbuf.append(strPattern, handledPosition, delimIndex - 1);
                        sbuf.append('{');
                        handledPosition = delimIndex + 1;
                    }
                } else {
                    // 正常替换占位符
                    sbuf.append(strPattern, handledPosition, delimIndex);
                    sbuf.append(Convert.utf8Str(argArray[argIndex]));
                    handledPosition = delimIndex + 2;  // 跳过{}
                }
            }

            // 添加剩余部分
            sbuf.append(strPattern, handledPosition, strPattern.length());
            return sbuf.toString();
        } else {
            return strPattern;
        }
    }
}
