package com.axin.common.utils;

import com.axin.common.core.text.Convert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet工具类
 * 
 * <p>提供便捷的方法来获取和操作HTTP请求/响应对象。</p>
 * 
 * <p>特性：</p>
 * <ul>
 *   <li>获取请求参数（支持默认值和类型转换）</li>
 *   <li>获取Request、Response、Session对象</li>
 *   <li>判断是否为Ajax请求</li>
 *   <li>渲染JSON字符串到响应</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * // 获取请求参数
 * String username = ServletUtils.getParameter("username");
 * Integer pageNum = ServletUtils.getParameterToInt("pageNum", 1);
 * 
 * // 判断是否为Ajax请求
 * if (ServletUtils.isAjaxRequest(request)) {
 *     // 处理Ajax请求
 * }
 * 
 * // 返回JSON数据
 * ServletUtils.renderString(response, jsonString);
 * </pre>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/19 16:49
 */
public class ServletUtils {

    public ServletUtils() {
    }

    /**
     * 获取请求参数值（同时支持GET和POST请求）
     * 
     * <p>优先从URL参数获取，如果获取不到则尝试从POST请求体中获取</p>
     * 
     * @param name 参数名
     * @return 参数值，如果不存在则返回null
     */
    public static String getParameter(String name) {
        HttpServletRequest request = getRequest();
        // 优先从URL参数获取
        String value = request.getParameter(name);
        if (value != null) {
            return value;
        }
        // 如果是POST请求且Content-Type为application/json，则从请求体获取
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            Map<String, Object> bodyParams = getRequestBodyParams(request);
            if (bodyParams != null && bodyParams.containsKey(name)) {
                Object obj = bodyParams.get(name);
                return obj != null ? obj.toString() : null;
            }
        }
        return null;
    }

    /**
     * 获取请求参数值（带默认值）
     * 
     * @param name 参数名
     * @param defaultValue 默认值
     * @return 参数值，如果不存在或转换失败则返回默认值
     */
    public static String getParameter(String name, String defaultValue) {
        return Convert.toStr(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取整型请求参数
     * 
     * @param name 参数名
     * @return 转换为整型的参数值，如果转换失败则返回null
     */
    public static Integer getParameterToInt(String name) {
        return Convert.toInt(getRequest().getParameter(name));
    }

    /**
     * 获取整型请求参数（带默认值）
     * 
     * @param name 参数名
     * @param defaultValue 默认值
     * @return 转换为整型的参数值，如果转换失败则返回默认值
     */
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        return Convert.toInt(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取当前请求的HttpServletRequest对象
     * 
     * @return HttpServletRequest对象
     */
    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    /**
     * 获取当前请求的HttpServletResponse对象
     * 
     * @return HttpServletResponse对象
     */
    public static HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    /**
     * 获取当前请求的HttpSession对象
     * 
     * @return HttpSession对象
     */
    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    /**
     * 获取ServletRequestAttributes对象
     * 
     * <p>从当前线程的RequestContextHolder中获取</p>
     * 
     * @return ServletRequestAttributes对象
     */
    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes)attributes;
    }

    /**
     * 将字符串渲染到客户端
     * 
     * <p>设置响应类型为application/json，编码为utf-8</p>
     * 
     * @param response HttpServletResponse对象
     * @param string 要渲染的字符串（通常为JSON字符串）
     * @return 始终返回null
     */
    public static String renderString(HttpServletResponse response, String string) {
        try {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取POST请求体中的JSON参数
     * 
     * <p>从请求体中解析JSON数据并转换为Map</p>
     * <p>注意：此方法会缓存请求体参数，避免重复解析</p>
     * 
     * @param request HttpServletRequest对象
     * @return 请求体参数Map，如果解析失败则返回null
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getRequestBodyParams(HttpServletRequest request) {
        // 从request attribute中获取缓存的参数
        Object cached = request.getAttribute("__body_params_cache__");
        if (cached != null) {
            return (Map<String, Object>) cached;
        }
        
        String contentType = request.getContentType();
        if (contentType == null || !contentType.contains("application/json")) {
            return null;
        }
        
        try {
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString();
            if (StringUtils.isEmpty(body)) {
                return null;
            }
            
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> params = mapper.readValue(body, Map.class);
            // 缓存到request attribute中
            request.setAttribute("__body_params_cache__", params);
            return params;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断是否为Ajax异步请求
     * 
     * <p>判断依据：</p>
     * <ol>
     *   <li>Accept请求头包含"application/json"</li>
     *   <li>X-Requested-With请求头为"XMLHttpRequest"</li>
     *   <li>URI以.json或.xml结尾</li>
     *   <li>__ajax参数为json或xml</li>
     * </ol>
     * 
     * @param request HttpServletRequest对象
     * @return 如果是Ajax请求返回true，否则返回false
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String accept = request.getHeader("accept");
        if (accept != null && accept.indexOf("application/json") != -1) {
            return true;
        } else {
            String xRequestedWith = request.getHeader("X-Requested-With");
            if (xRequestedWith != null && xRequestedWith.indexOf("XMLHttpRequest") != -1) {
                return true;
            } else {
                String uri = request.getRequestURI();
                if (StringUtils.inStringIgnoreCase(uri, new String[]{".json", ".xml"})) {
                    return true;
                } else {
                    String ajax = request.getParameter("__ajax");
                    return StringUtils.inStringIgnoreCase(ajax, new String[]{"json", "xml"});
                }
            }
        }
    }
}
