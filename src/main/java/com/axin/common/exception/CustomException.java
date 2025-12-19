package com.axin.common.exception;

/**
 * 自定义业务异常类
 * 
 * <p>用于封装业务逻辑中的异常情况，继承自RuntimeException，属于非检查型异常。</p>
 * 
 * <p>特性：</p>
 * <ul>
 *   <li>支持自定义错误码和错误消息</li>
 *   <li>支持异常链传递（包装原始异常）</li>
 *   <li>无需强制捕获，便于业务代码编写</li>
 *   <li>适用于业务校验失败、数据不合法等场景</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * // 仅包含错误消息
 * throw new CustomException("用户不存在");
 * 
 * // 包含错误码和错误消息
 * throw new CustomException("余额不足", 1001);
 * 
 * // 包装原始异常
 * throw new CustomException("数据库操作失败", originalException);
 * </pre>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/18 14:51
 */
public class CustomException extends RuntimeException{
    private static final long serialVersionUID = -5817808365151795266L;

    /**
     * 错误码
     */
    private Integer code;
    
    /**
     * 错误消息
     */
    private String message;

    /**
     * 构造一个仅包含错误消息的自定义异常
     * 
     * @param message 错误消息
     */
    public CustomException(String message) {
        this.message = message;
    }

    /**
     * 构造一个包含错误消息和错误码的自定义异常
     * 
     * @param message 错误消息
     * @param code 错误码，用于标识具体的错误类型
     */
    public CustomException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    /**
     * 构造一个包装原始异常的自定义异常
     * 
     * <p>用于在捕获底层异常后重新抛出业务异常，同时保留原始异常信息。</p>
     * 
     * @param message 错误消息
     * @param e 原始异常对象，用于异常链追踪
     */
    public CustomException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    /**
     * 获取错误码
     * 
     * @return 错误码，如果未设置则返回null
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 获取错误消息
     * 
     * @return 错误消息
     */
    @Override
    public String getMessage() {
        return message;
    }
}
