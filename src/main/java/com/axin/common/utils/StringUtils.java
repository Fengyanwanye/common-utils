package com.axin.common.utils;

import com.axin.common.core.text.StrFormatter;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * 
 * <p>扩展自Apache Commons Lang的StringUtils，提供更丰富的字符串处理功能。</p>
 * 
 * <p>特性：</p>
 * <ul>
 *   <li>空值判断：支持字符串、集合、数组、Map的空值检查</li>
 *   <li>字符串截取：支持负数索引的安全截取</li>
 *   <li>命名转换：驼峰命名与下划线命名互转</li>
 *   <li>集合转换：字符串分割转List/Set</li>
 *   <li>拼音转换：中文转拼音（全拼/首字母）</li>
 *   <li>格式化：支持占位符格式化</li>
 *   <li>安全操作：所有方法对null值都有良好的处理</li>
 * </ul>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/17 9:24
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    /**
     * 空字符串常量
     */
    private static final String NULLSTR = "";
    
    /**
     * 下划线分隔符
     */
    private static final char SEPARATOR = '_';

    /**
     * 构造函数
     */
    public StringUtils() {
    }

    /**
     * 返回非空值，如果value为null则返回defaultValue
     * 
     * <p>类似于SQL中的NVL函数或Java 9+的Objects.requireNonNullElse。</p>
     * 
     * @param <T> 泛型类型
     * @param value 待检查的值
     * @param defaultValue 默认值
     * @return 如果value不为null返回value，否则返回defaultValue
     */
    public static <T> T nvl(T value, T defaultValue) {
        return (T)(value != null ? value : defaultValue);
    }

    /**
     * 判断集合是否为空
     * 
     * @param coll 待判断的集合
     * @return 如果集合为null或没有元素返回true，否则返回false
     */
    public static boolean isEmpty(Collection<?> coll) {
        return isNull(coll) || coll.isEmpty();
    }

    /**
     * 判断集合是否不为空
     * 
     * @param coll 待判断的集合
     * @return 如果集合不为null且有元素返回true，否则返回false
     */
    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    /**
     * 判断对象数组是否为空
     * 
     * @param objects 待判断的对象数组
     * @return 如果数组为null或长度为0返回true，否则返回false
     */
    public static boolean isEmpty(Object[] objects) {
        return isNull(objects) || objects.length == 0;
    }

    /**
     * 判断对象数组是否不为空
     * 
     * @param objects 待判断的对象数组
     * @return 如果数组不为null且长度大于0返回true，否则返回false
     */
    public static boolean isNotEmpty(Object[] objects) {
        return !isEmpty(objects);
    }

    /**
     * 判断Map是否为空
     * 
     * @param map 待判断的Map
     * @return 如果Map为null或没有元素返回true，否则返回false
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return isNull(map) || map.isEmpty();
    }

    /**
     * 判断Map是否不为空
     * 
     * @param map 待判断的Map
     * @return 如果Map不为null且有元素返回true，否则返回false
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 判断字符串是否为空
     * 
     * <p>会先trim去除首尾空格，然后判断是否为空字符串。</p>
     * 
     * @param str 待判断的字符串
     * @return 如果字符串为null或trim后为空字符串返回true，否则返回false
     */
    public static boolean isEmpty(String str) {
        return isNull(str) || "".equals(str.trim());
    }

    /**
     * 判断字符串是否不为空
     * 
     * @param str 待判断的字符串
     * @return 如果字符串不为null且trim后不为空字符串返回true，否则返回false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断对象是否为null
     * 
     * @param object 待判断的对象
     * @return 如果对象为null返回true，否则返回false
     */
    public static boolean isNull(Object object) {
        return object == null;
    }

    /**
     * 判断对象是否不为null
     * 
     * @param object 待判断的对象
     * @return 如果对象不为null返回true，否则返回false
     */
    public static boolean isNotNull(Object object) {
        return !isNull(object);
    }

    /**
     * 判断对象是否为数组类型
     * 
     * @param object 待判断的对象
     * @return 如果对象不为null且是数组类型返回true，否则返回false
     */
    public static boolean isArray(Object object) {
        return isNotNull(object) && object.getClass().isArray();
    }

    /**
     * 去除字符串首尾空格
     * 
     * @param str 待处理的字符串
     * @return 去除首尾空格后的字符串，如果str为null则返回空字符串
     */
    public static String trim(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * 截取字符串（从指定位置到末尾）
     * 
     * <p>支持负数索引，负数表示从字符串末尾开始计算。</p>
     * <p>示例：</p>
     * <pre>
     * substring("hello", 2)   返回 "llo"
     * substring("hello", -2)  返回 "lo"
     * substring("hello", 10)  返回 ""
     * substring(null, 2)      返回 ""
     * </pre>
     * 
     * @param str 待截取的字符串
     * @param start 起始位置，支持负数
     * @return 截取后的字符串，如果str为null或起始位置超出范围返回空字符串
     */
    public static String substring(String str, int start) {
        if (str == null) {
            return "";
        } else {
            if (start < 0) {
                start += str.length();
            }

            if (start < 0) {
                start = 0;
            }

            return start > str.length() ? "" : str.substring(start);
        }
    }

    /**
     * 截取字符串（指定起始和结束位置）
     * 
     * <p>支持负数索引，负数表示从字符串末尾开始计算。</p>
     * <p>示例：</p>
     * <pre>
     * substring("hello", 1, 4)    返回 "ell"
     * substring("hello", -3, -1)  返回 "ll"
     * substring("hello", 0, 10)   返回 "hello"
     * substring(null, 1, 3)       返回 ""
     * </pre>
     * 
     * @param str 待截取的字符串
     * @param start 起始位置，支持负数
     * @param end 结束位置，支持负数
     * @return 截取后的字符串，如果str为null或起始位置大于结束位置返回空字符串
     */
    public static String substring(String str, int start, int end) {
        if (str == null) {
            return "";
        } else {
            if (end < 0) {
                end += str.length();
            }

            if (start < 0) {
                start += str.length();
            }

            if (end > str.length()) {
                end = str.length();
            }

            if (start > end) {
                return "";
            } else {
                if (start < 0) {
                    start = 0;
                }

                if (end < 0) {
                    end = 0;
                }

                return str.substring(start, end);
            }
        }
    }

    /**
     * 格式化字符串（占位符替换）
     * 
     * <p>使用{}作为占位符，按顺序替换参数。</p>
     * <p>示例：</p>
     * <pre>
     * format("用户{}登录，IP：{}", "admin", "192.168.1.1") 
     * 返回 "用户admin登录，IP：192.168.1.1"
     * </pre>
     * 
     * @param template 字符串模板
     * @param params 替换参数
     * @return 格式化后的字符串
     * @see StrFormatter#format(String, Object...)
     */
    public static String format(String template, Object... params) {
        return !isEmpty(params) && !isEmpty(template) ? StrFormatter.format(template, params) : template;
    }

    /**
     * 将字符串按分隔符分割转换为Set
     * 
     * <p>自动过滤空白元素并去重。</p>
     * 
     * @param str 待分割的字符串
     * @param sep 分隔符（正则表达式）
     * @return 字符串Set集合
     */
    public static final Set<String> str2Set(String str, String sep) {
        return new HashSet(str2List(str, sep, true, false));
    }

    /**
     * 将字符串按分隔符分割转换为List
     * 
     * <p>示例：</p>
     * <pre>
     * str2List("a,b,c", ",", false, true)  返回 ["a", "b", "c"]
     * str2List("a, ,c", ",", true, true)   返回 ["a", "c"] （过滤空白）
     * str2List(" a , b ", ",", false, true) 返回 ["a", "b"] （trim处理）
     * </pre>
     * 
     * @param str 待分割的字符串
     * @param sep 分隔符（正则表达式）
     * @param filterBlank 是否过滤空白元素
     * @param trim 是否对每个元素执行trim操作
     * @return 字符串List集合
     */
    public static final List<String> str2List(String str, String sep, boolean filterBlank, boolean trim) {
        List<String> list = new ArrayList();
        if (isEmpty(str)) {
            return list;
        } else if (filterBlank && isBlank(str)) {
            return list;
        } else {
            String[] split = str.split(sep);

            for(String string : split) {
                if (!filterBlank || !isBlank(string)) {
                    if (trim) {
                        string = string.trim();
                    }

                    list.add(string);
                }
            }

            return list;
        }
    }

    /**
     * 将驼峰命名转换为下划线命名
     * 
     * <p>示例：</p>
     * <pre>
     * toUnderScoreCase("userName")      返回 "user_name"
     * toUnderScoreCase("UserName")      返回 "user_name"
     * toUnderScoreCase("getUserName")   返回 "get_user_name"
     * toUnderScoreCase("getHTTPURL")    返回 "get_httpurl"
     * </pre>
     * 
     * @param str 驼峰命名的字符串
     * @return 下划线命名的字符串（全小写），如果str为null则返回null
     */
    public static String toUnderScoreCase(String str) {
        if (str == null) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            boolean preCharIsUpperCase = true;
            boolean curreCharIsUpperCase = true;
            boolean nexteCharIsUpperCase = true;

            for(int i = 0; i < str.length(); ++i) {
                char c = str.charAt(i);
                if (i > 0) {
                    preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
                } else {
                    preCharIsUpperCase = false;
                }

                curreCharIsUpperCase = Character.isUpperCase(c);
                if (i < str.length() - 1) {
                    nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
                }

                if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase) {
                    sb.append('_');
                } else if (i != 0 && !preCharIsUpperCase && curreCharIsUpperCase) {
                    sb.append('_');
                }

                sb.append(Character.toLowerCase(c));
            }

            return sb.toString();
        }
    }

    /**
     * 判断字符串是否在字符串数组中（忽略大小写）
     * 
     * <p>示例：</p>
     * <pre>
     * inStringIgnoreCase("abc", "ABC", "DEF")  返回 true
     * inStringIgnoreCase("xyz", "ABC", "DEF")  返回 false
     * </pre>
     * 
     * @param str 待查找的字符串
     * @param strs 字符串数组
     * @return 如果str在数组中存在（忽略大小写）返回true，否则返回false
     */
    public static boolean inStringIgnoreCase(String str, String... strs) {
        if (str != null && strs != null) {
            for(String s : strs) {
                if (str.equalsIgnoreCase(trim(s))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 将下划线命名转换为大驼峰命名（首字母大写）
     * 
     * <p>示例：</p>
     * <pre>
     * convertToCamelCase("user_name")       返回 "UserName"
     * convertToCamelCase("get_user_name")   返回 "GetUserName"
     * convertToCamelCase("username")        返回 "Username"
     * </pre>
     * 
     * @param name 下划线命名的字符串
     * @return 大驼峰命名的字符串（首字母大写），如果name为null或空返回空字符串
     */
    public static String convertToCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        if (name != null && !name.isEmpty()) {
            if (!name.contains("_")) {
                return name.substring(0, 1).toUpperCase() + name.substring(1);
            } else {
                String[] camels = name.split("_");

                for(String camel : camels) {
                    if (!camel.isEmpty()) {
                        result.append(camel.substring(0, 1).toUpperCase());
                        result.append(camel.substring(1).toLowerCase());
                    }
                }

                return result.toString();
            }
        } else {
            return "";
        }
    }

    /**
     * 将下划线命名转换为小驼峰命名（首字母小写）
     * 
     * <p>示例：</p>
     * <pre>
     * toCamelCase("user_name")       返回 "userName"
     * toCamelCase("get_user_name")   返回 "getUserName"
     * toCamelCase("USER_NAME")       返回 "userName"
     * </pre>
     * 
     * @param s 下划线命名的字符串
     * @return 小驼峰命名的字符串（首字母小写），如果s为null则返回null
     */
    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        } else {
            s = s.toLowerCase();
            StringBuilder sb = new StringBuilder(s.length());
            boolean upperCase = false;

            for(int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                if (c == '_') {
                    upperCase = true;
                } else if (upperCase) {
                    sb.append(Character.toUpperCase(c));
                    upperCase = false;
                } else {
                    sb.append(c);
                }
            }

            return sb.toString();
        }
    }

    /**
     * 从Map中根据key获取值并转换为字符串
     * 
     * @param key Map的key
     * @param map Map对象
     * @return key对应的值转换为字符串，如果map为null或key不存在返回空字符串
     */
    public static String getValueByKey(String key, Map<String, Object> map) {
        return map != null && map.get(key) != null ? map.get(key).toString() : "";
    }

    /**
     * 对象类型强制转换
     * 
     * <p>用于泛型类型的强制转换，避免出现unchecked警告。</p>
     * 
     * @param <T> 目标类型
     * @param obj 待转换的对象
     * @return 转换后的对象
     */
    public static <T> T cast(Object obj) {
        return (T)obj;
    }

    /**
     * 将中文转换为拼音
     * 
     * <p>示例：</p>
     * <pre>
     * keyword("张三", true)   返回 "zhangsan" （全拼）
     * keyword("张三", false)  返回 "zs" （首字母）
     * keyword("hello世界", true)  返回 "helloshijie" （中英文混合）
     * </pre>
     * 
     * @param yingwen 待转换的字符串（可包含中文、英文等）
     * @param full 是否返回全拼，true返回全拼，false返回首字母
     * @return 转换后的拼音字符串，如果yingwen为null或空返回空字符串
     */
    public static String keyword(String yingwen, boolean full) {
        String regExp = "[一-龥]+";
        StringBuffer py = new StringBuffer();
        if (yingwen != null && !"".equals(yingwen.trim())) {
            String pinyin = "";

            for(int i = 0; i < yingwen.length(); ++i) {
                char unit = yingwen.charAt(i);
                if (match(String.valueOf(unit), regExp)) {
                    pinyin = convertSingleyingwen2Pinyin(unit);
                    if (full) {
                        py.append(pinyin);
                    } else {
                        py.append(pinyin.charAt(0));
                    }
                } else {
                    py.append(unit);
                }
            }

            return py.toString();
        } else {
            return "";
        }
    }

    /**
     * 将单个中文字符转换为拼音
     * 
     * <p>内部使用pinyin4j库进行转换，不带声调。</p>
     * 
     * @param yingwen 中文字符
     * @return 该字符的拼音，如果转换失败返回空字符串
     */
    public static String convertSingleyingwen2Pinyin(char yingwen) {
        HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
        outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        StringBuffer py = new StringBuffer();

        try {
            String[] res = PinyinHelper.toHanyuPinyinStringArray(yingwen, outputFormat);
            py.append(res[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return py.toString();
    }

    /**
     * 使用正则表达式判断字符串是否匹配
     * 
     * <p>示例：</p>
     * <pre>
     * match("abc123", "\\d+")     返回 true （包含数字）
     * match("abcdef", "\\d+")     返回 false
     * match("张三", "[一-龥]+")   返回 true （包含中文）
     * </pre>
     * 
     * @param str 待匹配的字符串
     * @param regex 正则表达式
     * @return 如果字符串匹配正则表达式返回true，否则返回false
     */
    public static boolean match(String str, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }
}
