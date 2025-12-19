package com.axin.common.core.text;

import com.axin.common.utils.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Set;

/**
 * 类型转换工具类
 * 
 * <p>提供各种Java基本类型、包装类型、字符串之间的安全转换功能，转换失败时返回默认值而不抛出异常。</p>
 * 
 * <p>特性：</p>
 * <ul>
 *   <li>支持所有Java基本类型及其包装类的转换</li>
 *   <li>支持BigInteger、BigDecimal等大数类型转换</li>
 *   <li>支持枚举类型转换</li>
 *   <li>支持字符串数组转换</li>
 *   <li>支持字节数组与字符串互转</li>
 *   <li>支持全角半角字符转换</li>
 *   <li>支持数字大写转换（金额）</li>
 *   <li>转换失败时返回默认值，保证程序健壮性</li>
 * </ul>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/17 9:28
 */
public class Convert {
    /**
     * 构造函数
     */
    public Convert() {
    }

    /**
     * 将对象转换为字符串
     * 
     * @param value 被转换的值
     * @param defaultValue 转换失败或值为null时的默认值
     * @return 转换后的字符串，如果value为null则返回默认值
     */
    public static String toStr(Object value, String defaultValue) {
        if (null == value) {
            return defaultValue;
        } else {
            return value instanceof String ? (String)value : value.toString();
        }
    }

    /**
     * 将对象转换为字符串
     * 
     * <p>转换失败时返回null。</p>
     * 
     * @param value 被转换的值
     * @return 转换后的字符串，如果value为null则返回null
     */
    public static String toStr(Object value) {
        return toStr(value, (String)null);
    }

    /**
     * 将对象转换为字符
     * 
     * <p>如果对象是字符串，则取第一个字符。</p>
     * 
     * @param value 被转换的值
     * @param defaultValue 转换失败或值为null时的默认值
     * @return 转换后的字符，如果转换失败则返回默认值
     */
    public static Character toChar(Object value, Character defaultValue) {
        if (null == value) {
            return defaultValue;
        } else if (value instanceof Character) {
            return (Character)value;
        } else {
            String valueStr = toStr(value, (String)null);
            return StringUtils.isEmpty(valueStr) ? defaultValue : valueStr.charAt(0);
        }
    }

    /**
     * 将对象转换为字符
     * 
     * <p>转换失败时返回null。</p>
     * 
     * @param value 被转换的值
     * @return 转换后的字符，如果转换失败则返回null
     */
    public static Character toChar(Object value) {
        return toChar(value, (Character)null);
    }

    /**
     * 将对象转换为Byte
     * 
     * <p>支持Number类型和字符串类型的转换。</p>
     * 
     * @param value 被转换的值
     * @param defaultValue 转换失败或值为null时的默认值
     * @return 转换后的Byte值，如果转换失败则返回默认值
     */
    public static Byte toByte(Object value, Byte defaultValue) {
        if (value == null) {
            return defaultValue;
        } else if (value instanceof Byte) {
            return (Byte)value;
        } else if (value instanceof Number) {
            return ((Number)value).byteValue();
        } else {
            String valueStr = toStr(value, (String)null);
            if (StringUtils.isEmpty(valueStr)) {
                return defaultValue;
            } else {
                try {
                    return Byte.parseByte(valueStr);
                } catch (Exception var4) {
                    return defaultValue;
                }
            }
        }
    }

    /**
     * 将对象转换为Byte
     * 
     * <p>转换失败时返回null。</p>
     * 
     * @param value 被转换的值
     * @return 转换后的Byte值，如果转换失败则返回null
     */
    public static Byte toByte(Object value) {
        return toByte(value, (Byte)null);
    }

    /**
     * 将对象转换为Short
     * 
     * <p>支持Number类型和字符串类型的转换，字符串会先去除首尾空格。</p>
     * 
     * @param value 被转换的值
     * @param defaultValue 转换失败或值为null时的默认值
     * @return 转换后的Short值，如果转换失败则返回默认值
     */
    public static Short toShort(Object value, Short defaultValue) {
        if (value == null) {
            return defaultValue;
        } else if (value instanceof Short) {
            return (Short)value;
        } else if (value instanceof Number) {
            return ((Number)value).shortValue();
        } else {
            String valueStr = toStr(value, (String)null);
            if (StringUtils.isEmpty(valueStr)) {
                return defaultValue;
            } else {
                try {
                    return Short.parseShort(valueStr.trim());
                } catch (Exception var4) {
                    return defaultValue;
                }
            }
        }
    }

    /**
     * 将对象转换为Short
     * 
     * <p>转换失败时返回null。</p>
     * 
     * @param value 被转换的值
     * @return 转换后的Short值，如果转换失败则返回null
     */
    public static Short toShort(Object value) {
        return toShort(value, (Short)null);
    }

    /**
     * 将对象转换为Number
     * 
     * <p>使用NumberFormat进行解析，支持各种数字格式。</p>
     * 
     * @param value 被转换的值
     * @param defaultValue 转换失败或值为null时的默认值
     * @return 转换后的Number值，如果转换失败则返回默认值
     */
    public static Number toNumber(Object value, Number defaultValue) {
        if (value == null) {
            return defaultValue;
        } else if (value instanceof Number) {
            return (Number)value;
        } else {
            String valueStr = toStr(value, (String)null);
            if (StringUtils.isEmpty(valueStr)) {
                return defaultValue;
            } else {
                try {
                    return NumberFormat.getInstance().parse(valueStr);
                } catch (Exception var4) {
                    return defaultValue;
                }
            }
        }
    }

    /**
     * 将对象转换为Number
     * 
     * <p>转换失败时返回null。</p>
     * 
     * @param value 被转换的值
     * @return 转换后的Number值，如果转换失败则返回null
     */
    public static Number toNumber(Object value) {
        return toNumber(value, (Number)null);
    }

    /**
     * 将对象转换为Integer
     * 
     * <p>支持Number类型和字符串类型的转换，字符串会先去除首尾空格。</p>
     * 
     * @param value 被转换的值
     * @param defaultValue 转换失败或值为null时的默认值
     * @return 转换后的Integer值，如果转换失败则返回默认值
     */
    public static Integer toInt(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        } else if (value instanceof Integer) {
            return (Integer)value;
        } else if (value instanceof Number) {
            return ((Number)value).intValue();
        } else {
            String valueStr = toStr(value, (String)null);
            if (StringUtils.isEmpty(valueStr)) {
                return defaultValue;
            } else {
                try {
                    return Integer.parseInt(valueStr.trim());
                } catch (Exception var4) {
                    return defaultValue;
                }
            }
        }
    }

    /**
     * 将对象转换为Integer
     * 
     * <p>转换失败时返回null。</p>
     * 
     * @param value 被转换的值
     * @return 转换后的Integer值，如果转换失败则返回null
     */
    public static Integer toInt(Object value) {
        return toInt(value, (Integer)null);
    }

    /**
     * 将字符串转换为Integer数组
     * 
     * <p>默认使用逗号分隔。</p>
     * 
     * @param str 被转换的字符串
     * @return Integer数组
     */
    public static Integer[] toIntArray(String str) {
        return toIntArray(",", str);
    }

    /**
     * 将字符串转换为Long数组
     * 
     * <p>默认使用逗号分隔。</p>
     * 
     * @param str 被转换的字符串
     * @return Long数组
     */
    public static Long[] toLongArray(String str) {
        return toLongArray(",", str);
    }

    /**
     * 将字符串转换为Integer数组
     * 
     * <p>使用指定的分隔符分割字符串，然后将每个部分转换为Integer。</p>
     * <p>转换失败的元素将被设置为0。</p>
     * 
     * @param split 分隔符
     * @param str 被转换的字符串
     * @return Integer数组，如果字符串为空则返回空数组
     */
    public static Integer[] toIntArray(String split, String str) {
        if (StringUtils.isEmpty(str)) {
            return new Integer[0];
        } else {
            String[] arr = str.split(split);
            Integer[] ints = new Integer[arr.length];

            for(int i = 0; i < arr.length; ++i) {
                Integer v = toInt(arr[i], 0);
                ints[i] = v;
            }

            return ints;
        }
    }

    /**
     * 将字符串转换为Long数组
     * 
     * <p>使用指定的分隔符分割字符串，然后将每个部分转换为Long。</p>
     * <p>转换失败的元素将被设置为null。</p>
     * 
     * @param split 分隔符
     * @param str 被转换的字符串
     * @return Long数组，如果字符串为空则返回空数组
     */
    public static Long[] toLongArray(String split, String str) {
        if (StringUtils.isEmpty(str)) {
            return new Long[0];
        } else {
            String[] arr = str.split(split);
            Long[] longs = new Long[arr.length];

            for(int i = 0; i < arr.length; ++i) {
                Long v = toLong(arr[i], (Long)null);
                longs[i] = v;
            }

            return longs;
        }
    }

    /**
     * 将字符串转换为String数组
     * 
     * <p>默认使用逗号分隔。</p>
     * 
     * @param str 被转换的字符串
     * @return String数组
     */
    public static String[] toStrArray(String str) {
        return toStrArray(",", str);
    }

    /**
     * 将字符串转换为String数组
     * 
     * <p>使用指定的分隔符分割字符串。</p>
     * 
     * @param split 分隔符（正则表达式）
     * @param str 被转换的字符串
     * @return String数组
     */
    public static String[] toStrArray(String split, String str) {
        return str.split(split);
    }

    /**
     * 将对象转换为Long
     * 
     * <p>支持Number类型和字符串类型的转换，字符串会先去除首尾空格。</p>
     * <p>使用BigDecimal进行转换以避免精度丢失。</p>
     * 
     * @param value 被转换的值
     * @param defaultValue 转换失败或值为null时的默认值
     * @return 转换后的Long值，如果转换失败则返回默认值
     */
    public static Long toLong(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        } else if (value instanceof Long) {
            return (Long)value;
        } else if (value instanceof Number) {
            return ((Number)value).longValue();
        } else {
            String valueStr = toStr(value, (String)null);
            if (StringUtils.isEmpty(valueStr)) {
                return defaultValue;
            } else {
                try {
                    return (new BigDecimal(valueStr.trim())).longValue();
                } catch (Exception var4) {
                    return defaultValue;
                }
            }
        }
    }

    /**
     * 将对象转换为Long
     * 
     * <p>转换失败时返回null。</p>
     * 
     * @param value 被转换的值
     * @return 转换后的Long值，如果转换失败则返回null
     */
    public static Long toLong(Object value) {
        return toLong(value, (Long)null);
    }

    /**
     * 将对象转换为Double
     * 
     * <p>支持Number类型和字符串类型的转换，字符串会先去除首尾空格。</p>
     * <p>使用BigDecimal进行转换以提高精度。</p>
     * 
     * @param value 被转换的值
     * @param defaultValue 转换失败或值为null时的默认值
     * @return 转换后的Double值，如果转换失败则返回默认值
     */
    public static Double toDouble(Object value, Double defaultValue) {
        if (value == null) {
            return defaultValue;
        } else if (value instanceof Double) {
            return (Double)value;
        } else if (value instanceof Number) {
            return ((Number)value).doubleValue();
        } else {
            String valueStr = toStr(value, (String)null);
            if (StringUtils.isEmpty(valueStr)) {
                return defaultValue;
            } else {
                try {
                    return (new BigDecimal(valueStr.trim())).doubleValue();
                } catch (Exception var4) {
                    return defaultValue;
                }
            }
        }
    }

    /**
     * 将对象转换为Double
     * 
     * <p>转换失败时返回null。</p>
     * 
     * @param value 被转换的值
     * @return 转换后的Double值，如果转换失败则返回null
     */
    public static Double toDouble(Object value) {
        return toDouble(value, (Double)null);
    }

    /**
     * 将对象转换为Float
     * 
     * <p>支持Number类型和字符串类型的转换，字符串会先去除首尾空格。</p>
     * 
     * @param value 被转换的值
     * @param defaultValue 转换失败或值为null时的默认值
     * @return 转换后的Float值，如果转换失败则返回默认值
     */
    public static Float toFloat(Object value, Float defaultValue) {
        if (value == null) {
            return defaultValue;
        } else if (value instanceof Float) {
            return (Float)value;
        } else if (value instanceof Number) {
            return ((Number)value).floatValue();
        } else {
            String valueStr = toStr(value, (String)null);
            if (StringUtils.isEmpty(valueStr)) {
                return defaultValue;
            } else {
                try {
                    return Float.parseFloat(valueStr.trim());
                } catch (Exception var4) {
                    return defaultValue;
                }
            }
        }
    }

    /**
     * 将对象转换为Float
     * 
     * <p>转换失败时返回null。</p>
     * 
     * @param value 被转换的值
     * @return 转换后的Float值，如果转换失败则返回null
     */
    public static Float toFloat(Object value) {
        return toFloat(value, (Float)null);
    }

    /**
     * 将对象转换为Boolean
     * 
     * <p>支持多种布尔值表示形式（不区分大小写）：</p>
     * <ul>
     *   <li>true: "true", "yes", "ok", "1"</li>
     *   <li>false: "false", "no", "0"</li>
     * </ul>
     * 
     * @param value 被转换的值
     * @param defaultValue 转换失败或值为null时的默认值
     * @return 转换后的Boolean值，如果转换失败则返回默认值
     */
    public static Boolean toBool(Object value, Boolean defaultValue) {
        String valueStr = toStr(value, (String)null);
        if (value == null) {
            return defaultValue;
        } else if (value instanceof Boolean) {
            return (Boolean)value;
        } else if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        } else {
            switch (valueStr.trim().toLowerCase()) {
                case "true":
                    return true;
                case "false":
                    return false;
                case "yes":
                    return true;
                case "ok":
                    return true;
                case "no":
                    return false;
                case "1":
                    return true;
                case "0":
                    return false;
                default:
                    return defaultValue;
            }
        }
    }

    /**
     * 将对象转换为Boolean
     * 
     * <p>转换失败时返回null。</p>
     * 
     * @param value 被转换的值
     * @return 转换后的Boolean值，如果转换失败则返回null
     */
    public static Boolean toBool(Object value) {
        return toBool(value, (Boolean)null);
    }

    /**
     * 将对象转换为枚举类型
     * 
     * <p>如果值已经是目标枚举类型，则直接返回。</p>
     * <p>如果值是字符串，则通过枚举名称进行匹配。</p>
     * 
     * @param <E> 枚举类型
     * @param clazz 枚举类的Class对象
     * @param value 被转换的值
     * @param defaultValue 转换失败或值为null时的默认值
     * @return 转换后的枚举值，如果转换失败则返回默认值
     */
    public static <E extends Enum<E>> E toEnum(Class<E> clazz, Object value, E defaultValue) {
        if (value == null) {
            return defaultValue;
        } else if (clazz.isAssignableFrom(value.getClass())) {
            E myE = (E)(value);
            return myE;
        } else {
            String valueStr = toStr(value, (String)null);
            if (StringUtils.isEmpty(valueStr)) {
                return defaultValue;
            } else {
                try {
                    return (E)Enum.valueOf(clazz, valueStr);
                } catch (Exception var5) {
                    return defaultValue;
                }
            }
        }
    }

    /**
     * 将对象转换为枚举类型
     * 
     * <p>转换失败时返回null。</p>
     * 
     * @param <E> 枚举类型
     * @param clazz 枚举类的Class对象
     * @param value 被转换的值
     * @return 转换后的枚举值，如果转换失败则返回null
     */
    public static <E extends Enum<E>> E toEnum(Class<E> clazz, Object value) {
        return toEnum(clazz, value, (E) null);
    }

    /**
     * 将对象转换为BigInteger
     * 
     * <p>支持BigInteger、Long和字符串类型的转换。</p>
     * 
     * @param value 被转换的值
     * @param defaultValue 转换失败或值为null时的默认值
     * @return 转换后的BigInteger值，如果转换失败则返回默认值
     */
    public static BigInteger toBigInteger(Object value, BigInteger defaultValue) {
        if (value == null) {
            return defaultValue;
        } else if (value instanceof BigInteger) {
            return (BigInteger)value;
        } else if (value instanceof Long) {
            return BigInteger.valueOf((Long)value);
        } else {
            String valueStr = toStr(value, (String)null);
            if (StringUtils.isEmpty(valueStr)) {
                return defaultValue;
            } else {
                try {
                    return new BigInteger(valueStr);
                } catch (Exception var4) {
                    return defaultValue;
                }
            }
        }
    }

    /**
     * 将对象转换为BigInteger
     * 
     * <p>转换失败时返回null。</p>
     * 
     * @param value 被转换的值
     * @return 转换后的BigInteger值，如果转换失败则返回null
     */
    public static BigInteger toBigInteger(Object value) {
        return toBigInteger(value, (BigInteger)null);
    }

    /**
     * 将对象转换为BigDecimal
     * 
     * <p>支持BigDecimal、Long、Double、Integer和字符串类型的转换。</p>
     * <p>BigDecimal适用于需要精确计算的场景，如金融计算。</p>
     * 
     * @param value 被转换的值
     * @param defaultValue 转换失败或值为null时的默认值
     * @return 转换后的BigDecimal值，如果转换失败则返回默认值
     */
    public static BigDecimal toBigDecimal(Object value, BigDecimal defaultValue) {
        if (value == null) {
            return defaultValue;
        } else if (value instanceof BigDecimal) {
            return (BigDecimal)value;
        } else if (value instanceof Long) {
            return new BigDecimal((Long)value);
        } else if (value instanceof Double) {
            return new BigDecimal((Double)value);
        } else if (value instanceof Integer) {
            return new BigDecimal((Integer)value);
        } else {
            String valueStr = toStr(value, (String)null);
            if (StringUtils.isEmpty(valueStr)) {
                return defaultValue;
            } else {
                try {
                    return new BigDecimal(valueStr);
                } catch (Exception var4) {
                    return defaultValue;
                }
            }
        }
    }

    /**
     * 将对象转换为BigDecimal
     * 
     * <p>转换失败时返回null。</p>
     * 
     * @param value 被转换的值
     * @return 转换后的BigDecimal值，如果转换失败则返回null
     */
    public static BigDecimal toBigDecimal(Object value) {
        return toBigDecimal(value, (BigDecimal)null);
    }

    /**
     * 将对象转换为UTF-8编码的字符串
     * 
     * <p>适用于字节数组、ByteBuffer等需要指定编码的对象。</p>
     * 
     * @param obj 被转换的对象
     * @return UTF-8编码的字符串
     */
    public static String utf8Str(Object obj) {
        return str(obj, CharsetKit.CHARSET_UTF_8);
    }

    /**
     * 将对象转换为指定字符集的字符串
     * 
     * @param obj 被转换的对象
     * @param charsetName 字符集名称
     * @return 指定字符集编码的字符串
     */
    public static String str(Object obj, String charsetName) {
        return str(obj, Charset.forName(charsetName));
    }

    /**
     * 将对象转换为指定字符集的字符串
     * 
     * <p>支持以下类型的转换：</p>
     * <ul>
     *   <li>String: 直接返回</li>
     *   <li>byte[]: 使用指定字符集解码</li>
     *   <li>Byte[]: 使用指定字符集解码</li>
     *   <li>ByteBuffer: 使用指定字符集解码</li>
     *   <li>其他: 调用toString()方法</li>
     * </ul>
     * 
     * @param obj 被转换的对象
     * @param charset 字符集
     * @return 指定字符集编码的字符串，如果obj为null则返回null
     */
    public static String str(Object obj, Charset charset) {
        if (null == obj) {
            return null;
        } else if (obj instanceof String) {
            return (String)obj;
        } else if (!(obj instanceof byte[]) && !(obj instanceof Byte[])) {
            return obj instanceof ByteBuffer ? str((ByteBuffer)obj, charset) : obj.toString();
        } else {
            return str((Object)((Byte[])obj), (Charset)charset);
        }
    }

    /**
     * 将字节数组转换为字符串
     * 
     * <p>如果字符集名称为空，则使用系统默认字符集。</p>
     * 
     * @param bytes 字节数组
     * @param charset 字符集名称
     * @return 转换后的字符串
     */
    public static String str(byte[] bytes, String charset) {
        return str(bytes, StringUtils.isEmpty(charset) ? Charset.defaultCharset() : Charset.forName(charset));
    }

    /**
     * 将字节数组转换为字符串
     * 
     * @param data 字节数组
     * @param charset 字符集，为null时使用系统默认字符集
     * @return 转换后的字符串，如果data为null则返回null
     */
    public static String str(byte[] data, Charset charset) {
        if (data == null) {
            return null;
        } else {
            return null == charset ? new String(data) : new String(data, charset);
        }
    }

    /**
     * 将ByteBuffer转换为字符串
     * 
     * @param data ByteBuffer对象
     * @param charset 字符集名称
     * @return 转换后的字符串，如果data为null则返回null
     */
    public static String str(ByteBuffer data, String charset) {
        return data == null ? null : str(data, Charset.forName(charset));
    }

    /**
     * 将ByteBuffer转换为字符串
     * 
     * @param data ByteBuffer对象
     * @param charset 字符集，为null时使用系统默认字符集
     * @return 转换后的字符串
     */
    public static String str(ByteBuffer data, Charset charset) {
        if (null == charset) {
            charset = Charset.defaultCharset();
        }

        return charset.decode(data).toString();
    }

    /**
     * 将半角字符转换为全角字符
     * 
     * <p>半角转全角：</p>
     * <ul>
     *   <li>半角空格转换为全角空格（12288）</li>
     *   <li>其他半角字符（33-126）转换为全角字符（65281-65374）</li>
     * </ul>
     * 
     * @param input 输入字符串
     * @return 转换后的全角字符串
     */
    public static String toSBC(String input) {
        return toSBC(input, (Set)null);
    }

    /**
     * 将半角字符转换为全角字符
     * 
     * <p>可以指定不需要转换的字符集合。</p>
     * 
     * @param input 输入字符串
     * @param notConvertSet 不需要转换的字符集合，这些字符将保持原样
     * @return 转换后的全角字符串
     */
    public static String toSBC(String input, Set<Character> notConvertSet) {
        char[] c = input.toCharArray();

        for(int i = 0; i < c.length; ++i) {
            if (null == notConvertSet || !notConvertSet.contains(c[i])) {
                if (c[i] == ' ') {
                    c[i] = 12288;
                } else if (c[i] < 127) {
                    c[i] += 'ﻠ';
                }
            }
        }

        return new String(c);
    }

    /**
     * 将全角字符转换为半角字符
     * 
     * <p>全角转半角：</p>
     * <ul>
     *   <li>全角空格（12288）转换为半角空格</li>
     *   <li>其他全角字符（65281-65374）转换为半角字符（33-126）</li>
     * </ul>
     * 
     * @param input 输入字符串
     * @return 转换后的半角字符串
     */
    public static String toDBC(String input) {
        return toDBC(input, (Set)null);
    }

    /**
     * 将全角字符转换为半角字符
     * 
     * <p>可以指定不需要转换的字符集合。</p>
     * 
     * @param text 输入字符串
     * @param notConvertSet 不需要转换的字符集合，这些字符将保持原样
     * @return 转换后的半角字符串
     */
    public static String toDBC(String text, Set<Character> notConvertSet) {
        char[] c = text.toCharArray();

        for(int i = 0; i < c.length; ++i) {
            if (null == notConvertSet || !notConvertSet.contains(c[i])) {
                if (c[i] == 12288) {
                    c[i] = ' ';
                } else if (c[i] > '\uff00' && c[i] < '｟') {
                    c[i] -= 'ﻠ';
                }
            }
        }

        String returnString = new String(c);
        return returnString;
    }

    /**
     * 将数字金额转换为中文大写形式
     * 
     * <p>适用于金额的中文大写表示，如：123.45 → 壹佰贰拾叁元肆角伍分</p>
     * <p>转换规则：</p>
     * <ul>
     *   <li>整数部分：按照万、亿进位，使用壹贰叁肆伍陆柒捌玖</li>
     *   <li>小数部分：精确到分，使用角、分单位</li>
     *   <li>负数前加"负"字</li>
     *   <li>没有小数部分时显示"整"</li>
     * </ul>
     * 
     * @param n 数字金额
     * @return 中文大写金额字符串
     */
    public static String digitUppercase(double n) {
        String[] fraction = new String[]{"角", "分"};
        String[] digit = new String[]{"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String[][] unit = new String[][]{{"元", "万", "亿"}, {"", "拾", "佰", "仟"}};
        String head = n < (double)0.0F ? "负" : "";
        n = Math.abs(n);
        String s = "";

        for(int i = 0; i < fraction.length; ++i) {
            s = s + (digit[(int)(Math.floor(n * (double)10.0F * Math.pow((double)10.0F, (double)i)) % (double)10.0F)] + fraction[i]).replaceAll("(零.)+", "");
        }

        if (s.length() < 1) {
            s = "整";
        }

        int integerPart = (int)Math.floor(n);

        for(int i = 0; i < unit[0].length && integerPart > 0; ++i) {
            String p = "";

            for(int j = 0; j < unit[1].length && n > (double)0.0F; ++j) {
                p = digit[integerPart % 10] + unit[1][j] + p;
                integerPart /= 10;
            }

            s = p.replaceAll("(零.)*零$", "").replaceAll("^$", "零") + unit[0][i] + s;
        }

        return head + s.replaceAll("(零.)*零元", "元").replaceFirst("(零.)+", "").replaceAll("(零.)+", "零").replaceAll("^整$", "零元整");
    }
}
