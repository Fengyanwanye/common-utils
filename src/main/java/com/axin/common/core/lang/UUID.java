package com.axin.common.core.lang;

import com.axin.common.exception.UtilException;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * UUID生成工具类
 * 
 * <p>提供高性能的UUID生成功能，支持安全模式和快速模式。</p>
 * 
 * <p>特性：</p>
 * <ul>
 *   <li>支持标准UUID格式（带横线）</li>
 *   <li>支持简化UUID格式（无横线）</li>
 *   <li>支持安全随机数生成器（SecureRandom）</li>
 *   <li>支持快速随机数生成器（ThreadLocalRandom）</li>
 *   <li>支持基于名称的UUID生成（MD5）</li>
 * </ul>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/18 11:06
 */
public class UUID implements Serializable, Comparable<UUID> {

    private static final long serialVersionUID = -6143415439159388988L;
    
    /**
     * UUID的最高64位
     */
    private final long mostSigBits;
    
    /**
     * UUID的最低64位
     */
    private final long leastSigBits;

    /**
     * 根据字节数组构造UUID
     * 
     * @param data 16字节的数据数组
     */
    private UUID(byte[] data) {
        long msb = 0L;
        long lsb = 0L;

        assert data.length == 16 : "data must be 16 bytes in length";

        for(int i = 0; i < 8; ++i) {
            msb = msb << 8 | (long)(data[i] & 255);
        }

        for(int i = 8; i < 16; ++i) {
            lsb = lsb << 8 | (long)(data[i] & 255);
        }

        this.mostSigBits = msb;
        this.leastSigBits = lsb;
    }

    /**
     * 根据最高有效位和最低有效位构造UUID
     * 
     * @param mostSigBits UUID的最高64位
     * @param leastSigBits UUID的最低64位
     */
    public UUID(long mostSigBits, long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
    }

    /**
     * 获取快速UUID（非安全模式）
     * 
     * <p>使用{@link ThreadLocalRandom}生成随机数，性能更高但安全性较低。</p>
     * <p>适用于对安全性要求不高的场景，如日志追踪ID、临时标识等。</p>
     * 
     * @return UUID对象
     */
    public static UUID fastUUID() {
        return randomUUID(false);
    }

    /**
     * 获取安全随机UUID（安全模式）
     * 
     * <p>使用{@link SecureRandom}生成随机数，安全性高但性能相对较低。</p>
     * <p>适用于对安全性要求较高的场景，如会话ID、密钥生成等。</p>
     * 
     * @return UUID对象
     */
    public static UUID randomUUID() {
        return randomUUID(true);
    }

    /**
     * 根据指定的安全模式生成随机UUID
     * 
     * <p>生成符合RFC 4122规范的Version 4（随机）UUID。</p>
     * 
     * @param isSecure 是否使用安全随机数生成器
     *                 true: 使用{@link SecureRandom}，安全性高但性能较低
     *                 false: 使用{@link ThreadLocalRandom}，性能高但安全性较低
     * @return UUID对象
     */
    public static UUID randomUUID(boolean isSecure) {
        Random ng = (Random)(isSecure ? UUID.Holder.numberGenerator : getRandom());
        byte[] randomBytes = new byte[16];
        ng.nextBytes(randomBytes);
        randomBytes[6] = (byte)(randomBytes[6] & 15);
        randomBytes[6] = (byte)(randomBytes[6] | 64);
        randomBytes[8] = (byte)(randomBytes[8] & 63);
        randomBytes[8] = (byte)(randomBytes[8] | 128);
        return new UUID(randomBytes);
    }

    /**
     * 根据名称字节数组生成UUID（Version 3）
     * 
     * <p>使用MD5哈希算法对输入的字节数组进行哈希，生成固定的UUID。</p>
     * <p>相同的输入将始终生成相同的UUID，适用于需要根据特定名称生成唯一标识的场景。</p>
     * 
     * @param name 名称的字节数组
     * @return 基于名称生成的UUID对象
     * @throws InternalError 如果MD5算法不可用
     */
    public static UUID nameUUIDFromBytes(byte[] name) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException var3) {
            throw new InternalError("MD5 not supported");
        }

        byte[] md5Bytes = md.digest(name);
        md5Bytes[6] = (byte)(md5Bytes[6] & 15);
        md5Bytes[6] = (byte)(md5Bytes[6] | 48);
        md5Bytes[8] = (byte)(md5Bytes[8] & 63);
        md5Bytes[8] = (byte)(md5Bytes[8] | 128);
        return new UUID(md5Bytes);
    }

    /**
     * 从标准字符串格式解析UUID
     * 
     * <p>字符串必须符合标准UUID格式：xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx</p>
     * <p>其中x为16进制字符（0-9, a-f, A-F）。</p>
     * 
     * @param name UUID的字符串表示形式
     * @return UUID对象
     * @throws IllegalArgumentException 如果字符串格式不正确
     */
    public static UUID fromString(String name) {
        String[] components = name.split("-");
        if (components.length != 5) {
            throw new IllegalArgumentException("Invalid UUID string: " + name);
        } else {
            for(int i = 0; i < 5; ++i) {
                components[i] = "0x" + components[i];
            }

            long mostSigBits = Long.decode(components[0]);
            mostSigBits <<= 16;
            mostSigBits |= Long.decode(components[1]);
            mostSigBits <<= 16;
            mostSigBits |= Long.decode(components[2]);
            long leastSigBits = Long.decode(components[3]);
            leastSigBits <<= 48;
            leastSigBits |= Long.decode(components[4]);
            return new UUID(mostSigBits, leastSigBits);
        }
    }

    /**
     * 获取UUID的最低有效64位
     * 
     * @return UUID的最低64位
     */
    public long getLeastSignificantBits() {
        return this.leastSigBits;
    }

    /**
     * 获取UUID的最高有效64位
     * 
     * @return UUID的最高64位
     */
    public long getMostSignificantBits() {
        return this.mostSigBits;
    }

    /**
     * 获取UUID的版本号
     * 
     * <p>版本号说明：</p>
     * <ul>
     *   <li>1: 基于时间的UUID</li>
     *   <li>2: DCE安全UUID</li>
     *   <li>3: 基于名称的UUID（MD5）</li>
     *   <li>4: 随机UUID</li>
     *   <li>5: 基于名称的UUID（SHA-1）</li>
     * </ul>
     * 
     * @return UUID的版本号（1-5）
     */
    public int version() {
        return (int)(this.mostSigBits >> 12 & 15L);
    }

    /**
     * 获取UUID的变体号
     * 
     * <p>变体号描述了UUID的布局，本实现遵循RFC 4122规范。</p>
     * 
     * @return UUID的变体号
     */
    public int variant() {
        return (int)(this.leastSigBits >>> (int)(64L - (this.leastSigBits >>> 62)) & this.leastSigBits >> 63);
    }

    /**
     * 获取时间戳（仅适用于Version 1的UUID）
     * 
     * <p>返回的时间戳是自1582年10月15日午夜以来的100纳秒间隔数。</p>
     * 
     * @return 时间戳值
     * @throws UnsupportedOperationException 如果此UUID不是基于时间的UUID（Version 1）
     */
    public long timestamp() throws UnsupportedOperationException {
        this.checkTimeBase();
        return (this.mostSigBits & 4095L) << 48 | (this.mostSigBits >> 16 & 65535L) << 32 | this.mostSigBits >>> 32;
    }

    /**
     * 获取时钟序列值（仅适用于Version 1的UUID）
     * 
     * <p>时钟序列用于在时钟回拨或节点ID变化时保证UUID的唯一性。</p>
     * 
     * @return 时钟序列值
     * @throws UnsupportedOperationException 如果此UUID不是基于时间的UUID（Version 1）
     */
    public int clockSequence() throws UnsupportedOperationException {
        this.checkTimeBase();
        return (int)((this.leastSigBits & 4611404543450677248L) >>> 48);
    }

    /**
     * 获取节点值（仅适用于Version 1的UUID）
     * 
     * <p>节点值通常是生成UUID的机器的MAC地址的48位值。</p>
     * 
     * @return 节点值（48位）
     * @throws UnsupportedOperationException 如果此UUID不是基于时间的UUID（Version 1）
     */
    public long node() throws UnsupportedOperationException {
        this.checkTimeBase();
        return this.leastSigBits & 281474976710655L;
    }

    /**
     * 返回UUID的标准字符串表示形式
     * 
     * <p>格式为：xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx</p>
     * <p>其中x为16进制小写字符。</p>
     * 
     * @return UUID的标准字符串表示形式（36个字符，包含4个横线）
     */
    public String toString() {
        return this.toString(false);
    }

    /**
     * 返回UUID的字符串表示形式
     * 
     * @param isSimple 是否返回简化格式
     *                 true: 返回32位简化格式（无横线）
     *                 false: 返回36位标准格式（包含4个横线）
     * @return UUID的字符串表示形式
     */
    public String toString(boolean isSimple) {
        StringBuilder builder = new StringBuilder(isSimple ? 32 : 36);
        builder.append(digits(this.mostSigBits >> 32, 8));
        if (!isSimple) {
            builder.append('-');
        }

        builder.append(digits(this.mostSigBits >> 16, 4));
        if (!isSimple) {
            builder.append('-');
        }

        builder.append(digits(this.mostSigBits, 4));
        if (!isSimple) {
            builder.append('-');
        }

        builder.append(digits(this.leastSigBits >> 48, 4));
        if (!isSimple) {
            builder.append('-');
        }

        builder.append(digits(this.leastSigBits, 12));
        return builder.toString();
    }

    /**
     * 返回此UUID的哈希码
     * 
     * @return UUID的哈希码值
     */
    public int hashCode() {
        long hilo = this.mostSigBits ^ this.leastSigBits;
        return (int)(hilo >> 32) ^ (int)hilo;
    }

    /**
     * 比较此UUID与指定对象是否相等
     * 
     * <p>当且仅当参数不为null、是UUID类型，且包含相同的值时返回true。</p>
     * 
     * @param obj 要比较的对象
     * @return 如果对象相同则返回true，否则返回false
     */
    public boolean equals(Object obj) {
        if (null != obj && obj.getClass() == UUID.class) {
            UUID id = (UUID)obj;
            return this.mostSigBits == id.mostSigBits && this.leastSigBits == id.leastSigBits;
        } else {
            return false;
        }
    }

    /**
     * 将此UUID与指定的UUID进行比较
     * 
     * <p>首先比较最高有效位，如果相同则比较最低有效位。</p>
     * 
     * @param val 要比较的UUID
     * @return 如果此UUID小于、等于或大于指定UUID，则分别返回负整数、零或正整数
     */
    @Override
    public int compareTo(UUID val) {
        return this.mostSigBits < val.mostSigBits ? -1 : (this.mostSigBits > val.mostSigBits ? 1 : (this.leastSigBits < val.leastSigBits ? -1 : (this.leastSigBits > val.leastSigBits ? 1 : 0)));
    }

    /**
     * 返回指定长度的16进制字符串
     * 
     * @param val 要转换的长整型值
     * @param digits 16进制字符串的位数
     * @return 固定长度的16进制字符串
     */
    private static String digits(long val, int digits) {
        long hi = 1L << digits * 4;
        return Long.toHexString(hi | val & hi - 1L).substring(1);
    }

    /**
     * 检查此UUID是否为基于时间的UUID（Version 1）
     * 
     * @throws UnsupportedOperationException 如果此UUID不是Version 1
     */
    private void checkTimeBase() {
        if (this.version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        }
    }

    /**
     * 获取安全随机数生成器
     * 
     * <p>使用SHA1PRNG算法创建SecureRandom实例。</p>
     * 
     * @return SecureRandom实例
     * @throws UtilException 如果SHA1PRNG算法不可用
     */
    public static SecureRandom getSecureRandom() {
        try {
            return SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new UtilException(e);
        }
    }

    /**
     * 获取线程本地随机数生成器
     * 
     * <p>ThreadLocalRandom性能优于SecureRandom，但安全性较低。</p>
     * <p>适用于对安全性要求不高的场景。</p>
     * 
     * @return 当前线程的ThreadLocalRandom实例
     */
    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * 静态内部类，用于延迟加载SecureRandom实例
     * 
     * <p>使用Holder模式实现线程安全的单例，只有在首次调用时才会初始化SecureRandom。</p>
     */
    private static class Holder {
        /**
         * 安全随机数生成器单例
         */
        static final SecureRandom numberGenerator = UUID.getSecureRandom();

        private Holder() {
        }
    }
}
