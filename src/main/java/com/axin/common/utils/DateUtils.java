package com.axin.common.utils;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 * 
 * <p>扩展自Apache Commons Lang的DateUtils，提供更丰富的日期处理功能。</p>
 * 
 * <p>特性：</p>
 * <ul>
 *   <li>预定义常用日期格式常量</li>
 *   <li>日期与字符串的灵活转换</li>
 *   <li>支持多种日期格式的自动解析</li>
 *   <li>获取服务器启动时间</li>
 *   <li>计算时间差（天、小时、分钟）</li>
 * </ul>
 * 
 * <p>常用日期格式：</p>
 * <ul>
 *   <li>YYYY: 年份（如"2025"）</li>
 *   <li>YYYY_MM: 年月（如"2025-12"）</li>
 *   <li>YYYY_MM_DD: 年月日（如"2025-12-19"）</li>
 *   <li>YYYYMMDDHHMMSS: 紧凑格式日期时间（如"20251219163000"）</li>
 *   <li>YYYY_MM_DD_HH_MM_SS: 标准格式日期时间（如"2025-12-19 16:30:00"）</li>
 * </ul>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/19 16:29
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    /**
     * 年份格式：yyyy
     */
    public static String YYYY = "yyyy";
    
    /**
     * 年月格式：yyyy-MM
     */
    public static String YYYY_MM = "yyyy-MM";
    
    /**
     * 年月日格式：yyyy-MM-dd
     */
    public static String YYYY_MM_DD = "yyyy-MM-dd";
    
    /**
     * 紧凑日期时间格式：yyyyMMddHHmmss
     */
    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    
    /**
     * 标准日期时间格式：yyyy-MM-dd HH:mm:ss
     */
    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 日期解析格式数组
     * 
     * <p>用于parseDate方法尝试多种格式解析日期字符串</p>
     */
    private static String[] parsePatterns = new String[]{"yyyyMMdd", "yyyyMMdd HH:mm:ss", "yyyyMMdd HH:mm", "yyyyMM", "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM", "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    public DateUtils() {
    }

    /**
     * 获取当前日期对象
     * 
     * @return 当前日期时间的Date对象
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获取当前日期字符串（年月日）
     * 
     * @return 格式为yyyy-MM-dd的日期字符串，如"2025-12-19"
     */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    /**
     * 获取当前时间字符串（完整日期时间）
     * 
     * @return 格式为yyyy-MM-dd HH:mm:ss的日期时间字符串，如"2025-12-19 16:30:00"
     */
    public static final String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 获取当前时间字符串（紧凑格式）
     * 
     * @return 格式为yyyyMMddHHmmss的日期时间字符串，如"20251219163000"
     */
    public static final String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    /**
     * 获取当前时间的指定格式字符串
     * 
     * @param format 日期格式字符串，如"yyyy-MM-dd HH:mm:ss"
     * @return 按指定格式格式化后的当前时间字符串
     */
    public static final String dateTimeNow(String format) {
        return parseDateToStr(format, new Date());
    }

    /**
     * 将日期对象格式化为年月日字符串
     * 
     * @param date 日期对象
     * @return 格式为yyyy-MM-dd的日期字符串
     */
    public static final String dateTime(Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    /**
     * 将日期对象格式化为字符串
     * 
     * @param format 日期格式字符串
     * @param date 日期对象
     * @return 格式化后的日期字符串
     */
    public static final String parseDateToStr(String format, Date date) {
        return (new SimpleDateFormat(format)).format(date);
    }

    /**
     * 将字符串解析为日期对象
     * 
     * @param format 日期格式字符串
     * @param ts 日期字符串
     * @return 解析后的日期对象
     * @throws RuntimeException 如果解析失败
     */
    public static final Date dateTime(String format, String ts) {
        try {
            return (new SimpleDateFormat(format)).parse(ts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取日期路径字符串
     * 
     * <p>用于生成按日期分层的目录路径</p>
     * 
     * @return 格式为yyyy/MM/dd的路径字符串，如"2025/12/19"
     */
    public static final String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 获取紧凑格式的日期字符串
     * 
     * @return 格式为yyyyMMdd的日期字符串，如"20251219"
     */
    public static final String dateTime() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 自动解析日期字符串
     * 
     * <p>尝试使用多种预定义格式解析日期字符串</p>
     * 
     * @param str 日期字符串对象
     * @return 解析后的日期对象，如果解析失败或str为null则返回null
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        } else {
            try {
                return parseDate(str.toString(), parsePatterns);
            } catch (ParseException var2) {
                return null;
            }
        }
    }

    /**
     * 获取服务器启动时间
     * 
     * <p>通过JVM运行时信息获取服务器启动时间</p>
     * 
     * @return 服务器启动时间的Date对象
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算两个时间之间的时间差
     * 
     * <p>返回格式化的时间差字符串：X天X小时X分钟</p>
     * 
     * @param endDate 结束时间
     * @param nowDate 开始时间
     * @return 格式化的时间差字符串，如"1天2小时30分钟"
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 86400000L;  // 一天的毫秒数
        long nh = 3600000L;   // 一小时的毫秒数
        long nm = 60000L;      // 一分钟的毫秒数
        long diff = endDate.getTime() - nowDate.getTime();
        long day = diff / nd;
        long hour = diff % nd / nh;
        long min = diff % nd % nh / nm;
        return day + "天" + hour + "小时" + min + "分钟";
    }
}
