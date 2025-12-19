package com.axin.common.utils;

import com.axin.common.utils.spring.SpringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 国际化消息工具类
 * 
 * <p>提供国际化消息的获取功能，支持根据当前线程的语言环境自动获取对应的消息内容。</p>
 * 
 * <p>特性：</p>
 * <ul>
 *   <li>自动识别当前请求的语言环境（Locale）</li>
 *   <li>支持消息参数替换</li>
 *   <li>集成Spring的MessageSource机制</li>
 *   <li>简化国际化消息的调用方式</li>
 * </ul>
 * 
 * <p>使用前提：</p>
 * <ul>
 *   <li>需要在Spring容器中配置MessageSource Bean</li>
 *   <li>需要准备国际化资源文件（如messages_zh_CN.properties、messages_en_US.properties）</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * // 获取简单消息
 * String msg = MessageUtils.message("user.not.found");
 * 
 * // 获取带参数的消息
 * // 资源文件中：user.login.success=用户{0}登录成功，IP地址：{1}
 * String msg = MessageUtils.message("user.login.success", "张三", "192.168.1.100");
 * 
 * // 在异常中使用
 * throw new BaseException("user.age.invalid", new Object[]{age, minAge, maxAge});
 * </pre>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/18 15:08
 * @see org.springframework.context.MessageSource
 * @see org.springframework.context.i18n.LocaleContextHolder
 */
public class MessageUtils {

    /**
     * 构造函数
     */
    public MessageUtils() {
    }

    /**
     * 根据消息键和参数获取国际化消息
     * 
     * <p>该方法会自动获取当前线程的语言环境（Locale），然后从对应的资源文件中获取消息。</p>
     * <p>消息支持参数占位符，使用{0}、{1}、{2}等格式，参数会按顺序替换占位符。</p>
     * 
     * <p>示例：</p>
     * <pre>
     * // 资源文件配置：
     * // messages_zh_CN.properties: user.not.found=用户[{0}]不存在
     * // messages_en_US.properties: user.not.found=User [{0}] not found
     * 
     * // 调用：
     * String msg = MessageUtils.message("user.not.found", "admin");
     * // 中文环境下返回：用户[admin]不存在
     * // 英文环境下返回：User [admin] not found
     * </pre>
     * 
     * @param code 消息键（在资源文件中定义的key）
     * @param args 消息参数，用于替换消息模板中的占位符{0}、{1}等，可以传入多个参数
     * @return 国际化后的消息字符串
     * @throws org.springframework.context.NoSuchMessageException 如果消息键不存在
     */
    public static String message(String code, Object... args) {
        MessageSource messageSource = (MessageSource) SpringUtils.getBean(MessageSource.class);
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
