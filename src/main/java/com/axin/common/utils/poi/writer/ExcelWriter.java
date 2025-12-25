package com.axin.common.utils.poi.writer;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.List;

/**
 * Excel写入器接口
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/24
 */
public interface ExcelWriter<T> {

    /**
     * 导出数据到文件
     *
     * @param data      数据列表
     * @param sheetName sheet名称
     * @return 文件名
     * @throws Exception 导出异常
     */
    String write(List<T> data, String sheetName) throws Exception;

    /**
     * 导出数据到输出流
     *
     * @param data         数据列表
     * @param sheetName    sheet名称
     * @param outputStream 输出流
     * @throws Exception 导出异常
     */
    void write(List<T> data, String sheetName, OutputStream outputStream) throws Exception;

    /**
     * 导出数据到HTTP响应
     *
     * @param data      数据列表
     * @param sheetName sheet名称
     * @param response  HTTP响应
     * @throws Exception 导出异常
     */
    void write(List<T> data, String sheetName, HttpServletResponse response) throws Exception;

    /**
     * 生成导入模板
     *
     * @param sheetName sheet名称
     * @return 文件名
     * @throws Exception 生成异常
     */
    String writeTemplate(String sheetName) throws Exception;
}
