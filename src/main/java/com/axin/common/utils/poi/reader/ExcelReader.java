package com.axin.common.utils.poi.reader;

import java.io.InputStream;
import java.util.List;

/**
 * Excel读取器接口
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/24
 */
public interface ExcelReader<T> {

    /**
     * 从输入流读取Excel数据
     *
     * @param inputStream 输入流
     * @return 数据列表
     * @throws Exception 读取异常
     */
    List<T> read(InputStream inputStream) throws Exception;

    /**
     * 从输入流读取指定sheet的Excel数据
     *
     * @param inputStream 输入流
     * @param sheetName   sheet名称
     * @return 数据列表
     * @throws Exception 读取异常
     */
    List<T> read(InputStream inputStream, String sheetName) throws Exception;

    /**
     * 从输入流读取Excel数据，支持自定义表头位置和数据起始行
     *
     * @param inputStream 输入流
     * @param sheetName   sheet名称
     * @param headStart   表头起始行
     * @param headEnd     表头结束行
     * @param dataStart   数据起始行
     * @return 数据列表
     * @throws Exception 读取异常
     */
    List<T> read(InputStream inputStream, String sheetName, int headStart, int headEnd, int dataStart) throws Exception;
}
