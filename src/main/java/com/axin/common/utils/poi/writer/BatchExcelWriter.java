package com.axin.common.utils.poi.writer;

import java.io.Closeable;
import java.io.OutputStream;
import java.util.List;

/**
 * 分批Excel写入器接口
 * <p>
 * 用于大数据量分批导出，避免内存溢出
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/24
 */
public interface BatchExcelWriter<T> extends Closeable {

    /**
     * 写入一批数据
     *
     * @param batch 批次数据
     * @throws Exception 写入异常
     */
    void writeBatch(List<T> batch) throws Exception;

    /**
     * 完成写入并保存文件
     *
     * @return 文件名
     * @throws Exception 保存异常
     */
    String finish() throws Exception;

    /**
     * 完成写入并输出到流
     *
     * @param outputStream 输出流
     * @throws Exception 输出异常
     */
    void finish(OutputStream outputStream) throws Exception;

    /**
     * 获取已写入的数据行数
     *
     * @return 行数
     */
    int getWrittenRows();
}
