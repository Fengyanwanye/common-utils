package com.axin.common.exception;

/**
 * 工具类异常
 * 
 * <p>用于封装工具类执行过程中产生的异常，继承自RuntimeException，属于非检查型异常。</p>
 * 
 * <p>特性：</p>
 * <ul>
 *   <li>专门用于工具类方法的异常封装</li>
 *   <li>支持包装原始异常，保留完整的异常堆栈信息</li>
 *   <li>支持自定义错误消息</li>
 *   <li>无需强制捕获，简化工具类方法的调用</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>工具类方法执行失败时抛出</li>
 *   <li>包装底层检查型异常，转换为非检查型异常</li>
 *   <li>提供统一的工具类异常处理机制</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * // 包装原始异常
 * try {
 *     // ... 某些操作
 * } catch (IOException e) {
 *     throw new UtilException(e);
 * }
 * 
 * // 自定义错误消息
 * throw new UtilException("文件读取失败");
 * 
 * // 自定义消息并包装原始异常
 * throw new UtilException("JSON解析失败", jsonException);
 * </pre>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/18 14:13
 */
public class UtilException extends RuntimeException {
    private static final long serialVersionUID = 5983392101958246927L;

    /**
     * 构造一个包装原始异常的工具类异常
     * 
     * <p>使用原始异常的消息作为错误消息，并保留完整的异常链。</p>
     * <p>适用于需要将检查型异常转换为非检查型异常的场景。</p>
     * 
     * @param e 原始异常对象
     */
    public UtilException(Throwable e) {
        super(e.getMessage(), e);
    }

    /**
     * 构造一个仅包含错误消息的工具类异常
     * 
     * <p>适用于工具类方法执行失败，需要抛出自定义错误消息的场景。</p>
     * 
     * @param message 错误消息
     */
    public UtilException(String message) {
        super(message);
    }

    /**
     * 构造一个包含自定义错误消息和原始异常的工具类异常
     * 
     * <p>使用自定义消息替换原始异常的消息，但保留完整的异常堆栈信息。</p>
     * <p>适用于需要提供更友好的错误提示，同时保留底层异常信息的场景。</p>
     * 
     * @param message 自定义错误消息
     * @param throwable 原始异常对象，用于异常链追踪
     */
    public UtilException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
