package com.axin.framework.utils;


import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 数据源加解密工具类
 * 
 * <p>提供基于AES算法的数据源密码加解密功能，用于保护数据库连接信息的安全。</p>
 * 
 * <p>特性：</p>
 * <ul>
 *   <li>使用AES对称加密算法（128位）</li>
 *   <li>使用ECB模式和PKCS5Padding填充</li>
 *   <li>加密结果使用Base64 URL安全编码</li>
 *   <li>支持UTF-8字符编码</li>
 *   <li>自动处理密钥长度（使用MD5哈希生成16字节密钥）</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>数据库连接用户名、密码的加密存储</li>
 *   <li>配置文件中敏感信息的加密保护</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * // 加密
 * String encryptedPwd = DatasourceUtils.encrypt("123456");
 * 
 * // 解密
 * String originalPwd = DatasourceUtils.decrypt(encryptedPwd);
 * </pre>
 * 
 * <p><b>注意：</b></p>
 * <ul>
 *   <li>生产环境必须通过系统属性或环境变量配置密钥，不要使用默认密钥</li>
 *   <li>当前使用ECB模式，生产环境建议使用CBC或GCM模式以提高安全性</li>
 *   <li>确保JDK已安装JCE无限强度权限策略文件（JDK 8u161+默认已包含）</li>
 * </ul>
 * 
 * <p><b>密钥配置方式：</b></p>
 * <ul>
 *   <li>系统属性：java -Ddatasource.encrypt.key=your_custom_key ...</li>
 *   <li>环境变量（Windows）：set DATASOURCE_ENCRYPT_KEY=your_custom_key</li>
 *   <li>环境变量（Linux/Mac）：export DATASOURCE_ENCRYPT_KEY=your_custom_key</li>
 *   <li>环境变量 Docker：environment:
 *                        - DATASOURCE_ENCRYPT_KEY=prod_secret_2025=your_custom_key</li>
 *   <li>环境变量 Kubernetes Secret：env:
 *                                   - name: DATASOURCE_ENCRYPT_KEY
 *                                     valueFrom:
 *                                       secretKeyRef:
 *                                         name: datasource-secret
 *                                         key: your_custom_key</li>
 * </ul>
 * 
 * @author fuchuanxin
 * @version 2.0
 * @date 2025/9/23 10:19
 */
public class DatasourceUtils {

    /**
     * AES加密算法名称
     */
    private static final String AES = "AES";
    
    /**
     * AES加密算法完整模式（ECB模式，PKCS5填充）
     */
    private static final String AES_ALGORITHM = "AES/ECB/PKCS5Padding";
    
    /**
     * 默认加解密密钥（仅作为后备方案）
     * <p><b>安全警告：</b>生产环境请通过系统属性或环境变量设置密钥</p>
     */
    private static final String DEFAULT_KEY = "admin_dims_2025";
    
    /**
     * 系统属性中的密钥配置项名称
     */
    private static final String SYSTEM_PROPERTY_KEY = "datasource.encrypt.key";
    
    /**
     * 环境变量中的密钥配置项名称
     */
    private static final String ENV_KEY = "DATASOURCE_ENCRYPT_KEY";
    
    /**
     * 获取加解密密钥
     * 
     * <p>密钥获取优先级：</p>
     * <ol>
     *   <li>系统属性：-Ddatasource.encrypt.key=your_key</li>
     *   <li>环境变量：DATASOURCE_ENCRYPT_KEY=your_key</li>
     *   <li>默认值：admin_dims_2025（不推荐用于生产环境）</li>
     * </ol>
     * 
     * @return 加解密密钥
     */
    private static String getEncryptKey() {
        // 1. 优先从系统属性读取
        String key = System.getProperty(SYSTEM_PROPERTY_KEY);
        if (key != null && !key.trim().isEmpty()) {
            return key;
        }
        
        // 2. 从环境变量读取
        key = System.getenv(ENV_KEY);
        if (key != null && !key.trim().isEmpty()) {
            return key;
        }
        
        // 3. 使用默认值（打印警告）
        System.err.println("[WARNING] Using default encryption key. Please set encryption key via:" +
                "\n  - System property: -D" + SYSTEM_PROPERTY_KEY + "=your_key" +
                "\n  - Environment variable: " + ENV_KEY + "=your_key");
        return DEFAULT_KEY;
    }

    /**
     * 加密字符串
     * 
     * <p>使用AES算法对字符串进行加密，加密后使用Base64 URL安全编码。</p>
     * <p>密钥从系统属性或环境变量中读取。</p>
     * 
     * @param data 待加密的字符串
     * @return Base64编码后的加密字符串
     * @throws Exception 如果加密过程出现错误
     */
    public static String encrypt(String data) throws Exception {
        String key = getEncryptKey();
        byte[] bt = encrypt(data.getBytes(StandardCharsets.UTF_8), key.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64URLSafeString(bt);
    }

    /**
     * 加密字节数组（内部方法）
     *
     * <p>使用AES算法对字节数组进行加密。密钥通过MD5哈希处理为16字节。</p>
     *
     * @param data 待加密的字节数组
     * @param key 加密密钥字节数组
     * @return 加密后的字节数组
     * @throws Exception 如果加密过程出现错误
     */
    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // 使用MD5将密钥转换为16字节（128位）
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] keyBytes = md.digest(key);
        
        // 创建AES密钥
        SecretKey secretKey = new SecretKeySpec(keyBytes, AES);
        
        // 创建加密器
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        return cipher.doFinal(data);
    }

    /**
     * 解密字符串
     * 
     * <p>使用AES算法对Base64编码的加密字符串进行解密。</p>
     * <p>密钥从系统属性或环境变量中读取。</p>
     * 
     * @param data Base64编码的加密字符串
     * @return 解密后的原始字符串，如果data为null则返回null
     * @throws Exception 如果解密过程出现错误
     */
    public static String decrypt(String data) throws Exception {
        if (data == null) {
            return null;
        }
        String key = getEncryptKey();
        byte[] btf = Base64.decodeBase64(data);
        byte[] bt = decrypt(btf, key.getBytes(StandardCharsets.UTF_8));
        return new String(bt, StandardCharsets.UTF_8);
    }

    /**
     * 解密字节数组（内部方法）
     * 
     * <p>使用AES算法对字节数组进行解密。密钥通过MD5哈希处理为16字节。</p>
     * 
     * @param data 待解密的字节数组
     * @param key 解密密钥字节数组
     * @return 解密后的字节数组
     * @throws Exception 如果解密过程出现错误
     */
    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 使用MD5将密钥转换为16字节（128位）
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] keyBytes = md.digest(key);
        
        // 创建AES密钥
        SecretKey secretKey = new SecretKeySpec(keyBytes, AES);
        
        // 创建解密器
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        
        return cipher.doFinal(data);
    }

    /**
     * 测试方法
     * 
     * <p>用于生成加密后的用户名和密码，可配置到数据源配置文件中。</p>
     * <p>注意：升级到AES后，之前使用DES加密的密码需要重新生成。</p>
     * 
     * <p>使用方式：</p>
     * <pre>
     * # 通过系统属性设置密钥
     * java -Ddatasource.encrypt.key=your_custom_key -cp ... DatasourceUtils
     * 
     * # 通过环境变量设置密钥（Windows）
     * set DATASOURCE_ENCRYPT_KEY=your_custom_key
     * java -cp ... DatasourceUtils
     * 
     * # 通过环境变量设置密钥（Linux/Mac）
     * export DATASOURCE_ENCRYPT_KEY=your_custom_key
     * java -cp ... DatasourceUtils
     * </pre>
     * 
     * @param args 命令行参数（未使用）
     * @throws Exception 如果加密过程出现错误
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=== 数据源加密工具 ===");
        System.out.println("当前使用的密钥: " + getEncryptKey());
        System.out.println();
        
        // 加密测试
        String enUsr = encrypt("admin");
        String enPwd = encrypt("123456");

        System.out.println("加密后的用户名: " + enUsr);
        System.out.println("加密后的密码: " + enPwd);
        
        // 解密测试（验证加解密正确性）
        System.out.println();
        System.out.println("=== 解密验证 ===");
        System.out.println("用户名解密: " + decrypt(enUsr));
        System.out.println("密码解密: " + decrypt(enPwd));
    }
}
