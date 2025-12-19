package com.axin.common.exception;

import com.axin.common.utils.MessageUtils;
import com.axin.common.utils.StringUtils;

/**
 * 基础异常类
 * 
 * <p>系统所有自定义异常的基类，支持国际化消息和模块化异常管理。</p>
 * 
 * <p>特性：</p>
 * <ul>
 *   <li>支持国际化消息（通过消息code）</li>
 *   <li>支持默认消息</li>
 *   <li>支持消息参数替换</li>
 *   <li>支持模块化异常标识</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * // 使用国际化消息code
 * throw new BaseException("user.not.found", new Object[]{userId});
 * 
 * // 指定模块和国际化消息
 * throw new BaseException("UserModule", "user.disabled", new Object[]{username});
 * 
 * // 仅使用默认消息
 * throw new BaseException("用户不存在");
 * 
 * // 完整参数
 * throw new BaseException("OrderModule", "order.amount.invalid", 
 *                         new Object[]{orderId, amount}, "订单金额不合法");
 * </pre>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/18 15:01
 */
public class BaseException extends RuntimeException{
    private static final long serialVersionUID = 4068236469793295975L;

    /**
     * 所属模块
     */
    private String module;
    
    /**
     * 错误码（国际化消息key）
     */
    private String code;
    
    /**
     * 错误消息参数
     */
    private Object[] args;
    
    /**
     * 默认错误消息（当国际化消息不存在时使用）
     */
    private String defaultMessage;

    /**
     * 构造一个完整的基础异常
     * 
     * <p>包含所有参数：模块标识、国际化消息code、消息参数、默认消息。</p>
     * <p>优先使用国际化消息，如果国际化消息不存在则使用默认消息。</p>
     * 
     * @param module 所属模块标识，用于标识异常来源
     * @param code 错误码（国际化消息key），用于从资源文件中获取国际化消息
     * @param args 消息参数数组，用于替换消息模板中的占位符
     * @param defaultMessage 默认错误消息，当国际化消息不存在时使用
     */
    public BaseException(String module, String code, Object[] args, String defaultMessage) {
        this.module = module;
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;
    }

    /**
     * 构造一个包含模块、消息code和参数的基础异常
     * 
     * <p>不指定默认消息，完全依赖国际化消息配置。</p>
     * 
     * @param module 所属模块标识
     * @param code 错误码（国际化消息key）
     * @param args 消息参数数组
     */
    public BaseException(String module, String code, Object[] args) {
        this(module, code, args, (String)null);
    }

    /**
     * 构造一个包含模块和默认消息的基础异常
     * 
     * <p>不使用国际化消息，直接使用默认消息。</p>
     * <p>适用于不需要国际化或临时调试的场景。</p>
     * 
     * @param module 所属模块标识
     * @param defaultMessage 默认错误消息
     */
    public BaseException(String module, String defaultMessage) {
        this(module, (String)null, (Object[])null, defaultMessage);
    }

    /**
     * 构造一个包含消息code和参数的基础异常
     * 
     * <p>不指定模块标识，使用国际化消息。</p>
     * <p>适用于通用异常或模块信息不重要的场景。</p>
     * 
     * @param code 错误码（国际化消息key）
     * @param args 消息参数数组
     */
    public BaseException(String code, Object[] args) {
        this((String)null, code, args, (String)null);
    }

    /**
     * 构造一个仅包含默认消息的基础异常
     * 
     * <p>最简单的构造方式，不使用模块标识和国际化消息。</p>
     * <p>适用于简单场景或快速开发调试。</p>
     * 
     * @param defaultMessage 默认错误消息
     */
    public BaseException(String defaultMessage) {
        this((String)null, (String)null, (Object[])null, defaultMessage);
    }

    /**
     * 获取异常消息
     * 
     * <p>消息获取策略：</p>
     * <ol>
     *   <li>如果设置了code，优先从国际化资源文件中获取消息</li>
     *   <li>如果国际化消息不存在，使用defaultMessage</li>
     *   <li>如果都不存在，返回null</li>
     * </ol>
     * <p>国际化消息支持参数替换，使用args中的值替换消息模板中的占位符。</p>
     * 
     * @return 异常消息，优先返回国际化消息，其次返回默认消息
     */
    @Override
    public String getMessage() {
        String message = null;
        if (!StringUtils.isEmpty(code)) {
            message = MessageUtils.message(code, args);
        }

        if (message == null) {
            message = defaultMessage;
        }

        return message;
    }

    /**
     * 获取所属模块标识
     * 
     * @return 模块标识，如果未设置则返回null
     */
    public String getModule() {
        return module;
    }

    /**
     * 获取错误码（国际化消息key）
     * 
     * @return 错误码，用于从国际化资源文件中获取消息，如果未设置则返回null
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取消息参数数组
     * 
     * @return 消息参数数组，用于替换国际化消息模板中的占位符，如果未设置则返回null
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * 获取默认错误消息
     * 
     * @return 默认错误消息，当国际化消息不存在时使用，如果未设置则返回null
     */
    public String getDefaultMessage() {
        return defaultMessage;
    }
}
