package com.axin.common.utils.poi;

import com.axin.common.utils.poi.reader.BatchExcelReader;
import com.axin.common.utils.poi.reader.DefaultBatchExcelReader;
import com.axin.common.utils.poi.reader.DefaultExcelReader;
import com.axin.common.utils.poi.reader.ExcelReader;
import com.axin.common.utils.poi.writer.BatchExcelWriter;
import com.axin.common.utils.poi.writer.DefaultBatchExcelWriter;
import com.axin.common.utils.poi.writer.DefaultExcelWriter;
import com.axin.common.utils.poi.writer.ExcelWriter;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Excel门面类 - 提供简洁的Excel导入导出API
 * <p>
 * 使用示例：
 * <pre>
 * // 导入Excel
 * List<User> users = ExcelFacade.importExcel(inputStream, User.class);
 * 
 * // 导出Excel
 * String filename = ExcelFacade.exportExcel(userList, "用户列表", User.class);
 * 
 * // 导出到HTTP响应
 * ExcelFacade.exportExcel(userList, "用户列表", User.class, response);
 * </pre>
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/24
 */
public class ExcelFacade {

    // ==================== 导入相关方法 ====================

    /**
     * 导入Excel（读取第一个Sheet，默认第一行为表头）
     *
     * @param inputStream 输入流
     * @param clazz       实体类
     * @return 数据列表
     */
    public static <T> List<T> importExcel(InputStream inputStream, Class<T> clazz) throws Exception {
        ExcelReader<T> reader = new DefaultExcelReader<>(clazz);
        return reader.read(inputStream);
    }

    /**
     * 导入Excel（指定Sheet名称）
     *
     * @param inputStream 输入流
     * @param sheetName   Sheet名称
     * @param clazz       实体类
     * @return 数据列表
     */
    public static <T> List<T> importExcel(InputStream inputStream, String sheetName, Class<T> clazz) throws Exception {
        ExcelReader<T> reader = new DefaultExcelReader<>(clazz);
        return reader.read(inputStream, sheetName);
    }

    /**
     * 导入Excel（自定义表头和数据起始行）
     *
     * @param inputStream 输入流
     * @param sheetName   Sheet名称
     * @param headStart   表头起始行（从0开始）
     * @param headEnd     表头结束行
     * @param dataStart   数据起始行
     * @param clazz       实体类
     * @return 数据列表
     */
    public static <T> List<T> importExcel(InputStream inputStream, String sheetName, 
                                          int headStart, int headEnd, int dataStart, 
                                          Class<T> clazz) throws Exception {
        ExcelReader<T> reader = new DefaultExcelReader<>(clazz);
        return reader.read(inputStream, sheetName, headStart, headEnd, dataStart);
    }

    // ==================== 大数据量分批导入相关方法 ====================

    /**
     * 分批导入Excel（用于大数据量导入，避免内存溢出）
     * <p>
     * 使用示例：
     * <pre>
     * int totalRows = ExcelFacade.importExcelBatch(
     *     inputStream, 
     *     User.class, 
     *     1000,  // 每批1000条
     *     (batch, batchNum, totalRead) -> {
     *         // 处理当前批次数据
     *         userService.saveBatch(batch);
     *         log.info("已处理第{}批，共{}条数据", batchNum, totalRead);
     *         return true;  // 返回true继续读取，false停止
     *     }
     * );
     * log.info("导入完成，总共{}条数据", totalRows);
     * </pre>
     *
     * @param inputStream 输入流
     * @param clazz       实体类
     * @param batchSize   每批数据量
     * @param callback    批处理回调函数
     * @return 总共读取的数据行数
     */
    public static <T> int importExcelBatch(InputStream inputStream, Class<T> clazz, 
                                           int batchSize, BatchExcelReader.BatchCallback<T> callback) throws Exception {
        BatchExcelReader<T> reader = new DefaultBatchExcelReader<>(clazz);
        return reader.readBatch(inputStream, batchSize, callback);
    }

    /**
     * 分批导入Excel（指定Sheet名称）
     *
     * @param inputStream 输入流
     * @param sheetName   Sheet名称
     * @param clazz       实体类
     * @param batchSize   每批数据量
     * @param callback    批处理回调函数
     * @return 总共读取的数据行数
     */
    public static <T> int importExcelBatch(InputStream inputStream, String sheetName, 
                                           Class<T> clazz, int batchSize, 
                                           BatchExcelReader.BatchCallback<T> callback) throws Exception {
        BatchExcelReader<T> reader = new DefaultBatchExcelReader<>(clazz);
        return reader.readBatch(inputStream, sheetName, batchSize, callback);
    }

    /**
     * 分批导入Excel（自定义表头和数据起始行）
     *
     * @param inputStream 输入流
     * @param sheetName   Sheet名称
     * @param headStart   表头起始行（从0开始）
     * @param headEnd     表头结束行
     * @param dataStart   数据起始行
     * @param clazz       实体类
     * @param batchSize   每批数据量
     * @param callback    批处理回调函数
     * @return 总共读取的数据行数
     */
    public static <T> int importExcelBatch(InputStream inputStream, String sheetName, 
                                           int headStart, int headEnd, int dataStart,
                                           Class<T> clazz, int batchSize, 
                                           BatchExcelReader.BatchCallback<T> callback) throws Exception {
        BatchExcelReader<T> reader = new DefaultBatchExcelReader<>(clazz);
        return reader.readBatch(inputStream, sheetName, headStart, headEnd, dataStart, batchSize, callback);
    }

    /**
     * 创建分批导入Reader（用于大数据量分批导入）
     * <p>
     * 使用示例：
     * <pre>
     * try (BatchExcelReader<User> reader = ExcelFacade.createBatchReader(User.class)) {
     *     reader.readBatch(inputStream, 1000, (batch, batchNum, totalRead) -> {
     *         userService.saveBatch(batch);
     *         return true;
     *     });
     * }
     * </pre>
     *
     * @param clazz 实体类
     * @return BatchExcelReader实例
     */
    public static <T> BatchExcelReader<T> createBatchReader(Class<T> clazz) {
        return new DefaultBatchExcelReader<>(clazz);
    }

    // ==================== 导出相关方法 ====================

    /**
     * 导出Excel到文件
     *
     * @param data      数据列表
     * @param sheetName Sheet名称
     * @param clazz     实体类
     * @return 文件名
     */
    public static <T> String exportExcel(List<T> data, String sheetName, Class<T> clazz) throws Exception {
        ExcelWriter<T> writer = new DefaultExcelWriter<>(clazz);
        return writer.write(data, sheetName);
    }

    /**
     * 导出Excel到输出流
     *
     * @param data         数据列表
     * @param sheetName    Sheet名称
     * @param clazz        实体类
     * @param outputStream 输出流
     */
    public static <T> void exportExcel(List<T> data, String sheetName, Class<T> clazz, 
                                       OutputStream outputStream) throws Exception {
        ExcelWriter<T> writer = new DefaultExcelWriter<>(clazz);
        writer.write(data, sheetName, outputStream);
    }

    /**
     * 导出Excel到HTTP响应
     *
     * @param data      数据列表
     * @param sheetName Sheet名称
     * @param clazz     实体类
     * @param response  HTTP响应
     */
    public static <T> void exportExcel(List<T> data, String sheetName, Class<T> clazz, 
                                       HttpServletResponse response) throws Exception {
        ExcelWriter<T> writer = new DefaultExcelWriter<>(clazz);
        writer.write(data, sheetName, response);
    }

    // ==================== 分批导出相关方法 ====================

    /**
     * 创建分批导出Writer（用于大数据量分批导出）
     * <p>
     * 使用示例：
     * <pre>
     * try (BatchExcelWriter<User> writer = ExcelFacade.createBatchWriter(User.class, "用户列表")) {
     *     // 分批写入数据
     *     for (int i = 0; i < totalPages; i++) {
     *         List<User> batch = userService.getPage(i, 1000);
     *         writer.writeBatch(batch);
     *     }
     *     // 完成并保存
     *     String filename = writer.finish();
     * }
     * </pre>
     *
     * @param clazz     实体类
     * @param sheetName Sheet名称
     * @return BatchExcelWriter实例
     */
    public static <T> BatchExcelWriter<T> createBatchWriter(Class<T> clazz, String sheetName) {
        return new DefaultBatchExcelWriter<>(clazz, sheetName);
    }

    /**
     * 生成Excel导入模板
     *
     * @param sheetName Sheet名称
     * @param clazz     实体类
     * @return 文件名
     */
    public static <T> String exportTemplate(String sheetName, Class<T> clazz) throws Exception {
        ExcelWriter<T> writer = new DefaultExcelWriter<>(clazz);
        return writer.writeTemplate(sheetName);
    }

    /**
     * 生成Excel导入模板到输出流
     *
     * @param sheetName    Sheet名称
     * @param clazz        实体类
     * @param outputStream 输出流
     */
    public static <T> void exportTemplate(String sheetName, Class<T> clazz, 
                                          OutputStream outputStream) throws Exception {
        ExcelWriter<T> writer = new DefaultExcelWriter<>(clazz);
        writer.write(null, sheetName, outputStream);
    }

    /**
     * 生成Excel导入模板到HTTP响应
     *
     * @param sheetName Sheet名称
     * @param clazz     实体类
     * @param response  HTTP响应
     */
    public static <T> void exportTemplate(String sheetName, Class<T> clazz, 
                                          HttpServletResponse response) throws Exception {
        ExcelWriter<T> writer = new DefaultExcelWriter<>(clazz);
        writer.write(null, sheetName, response);
    }

    // ==================== 工具方法 ====================

    /**
     * 创建ExcelReader实例
     *
     * @param clazz 实体类
     * @return ExcelReader实例
     */
    public static <T> ExcelReader<T> createReader(Class<T> clazz) {
        return new DefaultExcelReader<>(clazz);
    }

    /**
     * 创建ExcelWriter实例
     *
     * @param clazz 实体类
     * @return ExcelWriter实例
     */
    public static <T> ExcelWriter<T> createWriter(Class<T> clazz) {
        return new DefaultExcelWriter<>(clazz);
    }
}
