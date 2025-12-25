package com.axin.common.utils.poi.reader;

import java.io.Closeable;
import java.io.InputStream;
import java.util.List;

/**
 * 分批Excel读取器接口
 * <p>
 * 用于大数据量分批导入，避免内存溢出
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/24
 */
public interface BatchExcelReader<T> extends Closeable {

    /**
     * 分批读取回调接口
     *
     * @param <T> 实体类型
     */
    @FunctionalInterface
    interface BatchCallback<T> {
        /**
         * 处理一批数据
         *
         * @param batch     当前批次数据
         * @param batchNum  批次号（从1开始）
         * @param totalRead 已读取总行数
         * @return 是否继续读取，返回false则停止读取
         */
        boolean process(List<T> batch, int batchNum, int totalRead);
    }

    /**
     * 分批读取Excel数据（使用回调处理）
     *
     * @param inputStream 输入流
     * @param batchSize   每批数据量
     * @param callback    批处理回调
     * @return 总共读取的数据行数
     * @throws Exception 读取异常
     */
    int readBatch(InputStream inputStream, int batchSize, BatchCallback<T> callback) throws Exception;

    /**
     * 分批读取指定Sheet的Excel数据
     *
     * @param inputStream 输入流
     * @param sheetName   Sheet名称
     * @param batchSize   每批数据量
     * @param callback    批处理回调
     * @return 总共读取的数据行数
     * @throws Exception 读取异常
     */
    int readBatch(InputStream inputStream, String sheetName, int batchSize, BatchCallback<T> callback) throws Exception;

    /**
     * 分批读取Excel数据，支持自定义表头位置和数据起始行
     *
     * @param inputStream 输入流
     * @param sheetName   Sheet名称
     * @param headStart   表头起始行
     * @param headEnd     表头结束行
     * @param dataStart   数据起始行
     * @param batchSize   每批数据量
     * @param callback    批处理回调
     * @return 总共读取的数据行数
     * @throws Exception 读取异常
     */
    int readBatch(InputStream inputStream, String sheetName, int headStart, int headEnd, 
                  int dataStart, int batchSize, BatchCallback<T> callback) throws Exception;

    /**
     * 获取已读取的数据行数
     *
     * @return 行数
     */
    int getReadRows();
}
