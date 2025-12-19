package com.axin.common.core.text;

import com.axin.common.utils.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 字符集工具类
 * 
 * <p>提供字符集转换、字符集常量定义等功能，简化字符编码相关操作。</p>
 * 
 * <p>特性：</p>
 * <ul>
 *   <li>预定义常用字符集常量（UTF-8、GBK、ISO-8859-1）</li>
 *   <li>支持字符串编码转换</li>
 *   <li>支持获取系统默认字符集</li>
 *   <li>字符集名称安全解析</li>
 * </ul>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/17 9:30
 */
public class CharsetKit {
    /**
     * ISO-8859-1字符集名称
     */
    public static final String ISO_8859_1 = "ISO-8859-1";
    
    /**
     * UTF-8字符集名称
     */
    public static final String UTF_8 = "UTF-8";
    
    /**
     * GBK字符集名称
     */
    public static final String GBK = "GBK";
    
    /**
     * ISO-8859-1字符集对象
     */
    public static final Charset CHARSET_ISO_8859_1 = Charset.forName("ISO-8859-1");
    
    /**
     * UTF-8字符集对象
     */
    public static final Charset CHARSET_UTF_8 = Charset.forName("UTF-8");
    
    /**
     * GBK字符集对象
     */
    public static final Charset CHARSET_GBK = Charset.forName("GBK");

    /**
     * 构造函数
     */
    public CharsetKit() {
    }

    /**
     * 根据字符集名称获取Charset对象
     * 
     * <p>如果字符集名称为空或null，则返回系统默认字符集。</p>
     * 
     * @param charset 字符集名称，可以为null或空字符串
     * @return Charset对象，如果参数为空则返回系统默认字符集
     */
    public static Charset charset(String charset) {
        return StringUtils.isEmpty(charset) ? Charset.defaultCharset() : Charset.forName(charset);
    }

    /**
     * 字符串编码转换（使用字符集名称）
     * 
     * <p>将字符串从源字符集编码转换为目标字符集编码。</p>
     * 
     * @param source 源字符串
     * @param srcCharset 源字符集名称
     * @param destCharset 目标字符集名称
     * @return 转换后的字符串
     */
    public static String convert(String source, String srcCharset, String destCharset) {
        return convert(source, Charset.forName(srcCharset), Charset.forName(destCharset));
    }

    /**
     * 字符串编码转换（使用Charset对象）
     * 
     * <p>将字符串从源字符集编码转换为目标字符集编码。</p>
     * <p>默认行为：</p>
     * <ul>
     *   <li>源字符集为null时，默认使用ISO-8859-1</li>
     *   <li>目标字符集为null时，默认使用UTF-8</li>
     *   <li>源字符串为空或两个字符集相同时，直接返回源字符串</li>
     * </ul>
     * 
     * @param source 源字符串
     * @param srcCharset 源字符集，为null时默认使用ISO-8859-1
     * @param destCharset 目标字符集，为null时默认使用UTF-8
     * @return 转换后的字符串，如果源字符串为空或字符集相同则返回原字符串
     */
    public static String convert(String source, Charset srcCharset, Charset destCharset) {
        if (null == srcCharset) {
            srcCharset = StandardCharsets.ISO_8859_1;
        }

        if (null == destCharset) {
            destCharset = StandardCharsets.UTF_8;
        }

        return !StringUtils.isEmpty(source) && !srcCharset.equals(destCharset) ? new String(source.getBytes(srcCharset), destCharset) : source;
    }

    /**
     * 获取系统默认字符集名称
     * 
     * <p>返回当前JVM运行环境的默认字符集名称。</p>
     * <p>该值取决于操作系统、JVM配置等因素，常见值有：</p>
     * <ul>
     *   <li>Windows中文系统：GBK</li>
     *   <li>Linux/Unix系统：UTF-8</li>
     *   <li>macOS系统：UTF-8</li>
     * </ul>
     * 
     * @return 系统默认字符集的名称
     */
    public static String systemCharset() {
        return Charset.defaultCharset().name();
    }
}
