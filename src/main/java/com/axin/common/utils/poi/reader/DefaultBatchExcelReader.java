package com.axin.common.utils.poi.reader;

import com.axin.common.utils.StringUtils;
import com.axin.common.utils.poi.handler.CellValueHandler;
import com.axin.common.utils.reflect.ReflectUtils;
import com.axin.framework.aspectj.lang.annotation.Excel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 默认分批Excel读取器实现
 * <p>
 * 使用流式处理，边读边处理，避免一次性加载所有数据到内存
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/24
 */
public class DefaultBatchExcelReader<T> implements BatchExcelReader<T> {

    private static final Logger log = LoggerFactory.getLogger(DefaultBatchExcelReader.class);

    private final Class<T> clazz;
    private final CellValueHandler cellValueHandler;
    private int totalReadRows = 0;
    private Workbook workbook;

    public DefaultBatchExcelReader(Class<T> clazz) {
        this.clazz = clazz;
        this.cellValueHandler = new CellValueHandler();
    }

    @Override
    public int readBatch(InputStream inputStream, int batchSize, BatchCallback<T> callback) throws Exception {
        return readBatch(inputStream, StringUtils.EMPTY, 0, 0, 1, batchSize, callback);
    }

    @Override
    public int readBatch(InputStream inputStream, String sheetName, int batchSize, BatchCallback<T> callback) throws Exception {
        return readBatch(inputStream, sheetName, 0, 0, 1, batchSize, callback);
    }

    @Override
    public int readBatch(InputStream inputStream, String sheetName, int headStart, int headEnd,
                         int dataStart, int batchSize, BatchCallback<T> callback) throws Exception {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("批次大小必须大于0");
        }
        if (callback == null) {
            throw new IllegalArgumentException("回调函数不能为null");
        }

        try {
            workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = getSheet(workbook, sheetName);

            if (sheet == null) {
                throw new IOException("文件sheet不存在");
            }

            return readDataFromSheetBatch(sheet, headStart, headEnd, dataStart, batchSize, callback);
        } finally {
            close();
        }
    }

    @Override
    public int getReadRows() {
        return totalReadRows;
    }

    @Override
    public void close() throws IOException {
        if (workbook != null) {
            try {
                workbook.close();
            } catch (IOException e) {
                log.error("关闭Workbook失败", e);
            }
        }
    }

    /**
     * 获取Sheet
     */
    private Sheet getSheet(Workbook workbook, String sheetName) {
        if (StringUtils.isNotEmpty(sheetName)) {
            return workbook.getSheet(sheetName);
        }
        return workbook.getSheetAt(0);
    }

    /**
     * 从Sheet分批读取数据
     */
    private int readDataFromSheetBatch(Sheet sheet, int headStart, int headEnd, int dataStart,
                                       int batchSize, BatchCallback<T> callback) throws Exception {
        // 获取列映射关系
        Map<String, Integer> cellMap = buildCellMap(sheet, headStart, headEnd);

        // 获取字段映射关系
        Map<Integer, Field> fieldsMap = buildFieldsMap(cellMap);

        int rows = sheet.getPhysicalNumberOfRows();
        List<T> batch = new ArrayList<>(batchSize);
        int batchNum = 0;
        totalReadRows = 0;

        log.info("开始分批读取Excel，总行数: {}, 数据起始行: {}, 批次大小: {}", rows, dataStart, batchSize);

        for (int i = dataStart; i < rows; i++) {
            Row row = sheet.getRow(i);
            T entity = readRowData(row, fieldsMap);

            if (entity != null) {
                batch.add(entity);
                totalReadRows++;

                // 达到批次大小，执行回调
                if (batch.size() >= batchSize) {
                    batchNum++;
                    log.debug("处理第{}批数据，数量: {}", batchNum, batch.size());

                    boolean continueRead = callback.process(new ArrayList<>(batch), batchNum, totalReadRows);
                    batch.clear();

                    if (!continueRead) {
                        log.info("回调函数返回false，停止读取");
                        break;
                    }
                }
            }
        }

        // 处理最后一批不足批次大小的数据
        if (!batch.isEmpty()) {
            batchNum++;
            log.debug("处理最后一批数据，数量: {}", batch.size());
            callback.process(batch, batchNum, totalReadRows);
        }

        log.info("Excel分批读取完成，总批次: {}, 总行数: {}", batchNum, totalReadRows);
        return totalReadRows;
    }

    /**
     * 构建单元格映射（表头名称 -> 列索引）
     */
    private Map<String, Integer> buildCellMap(Sheet sheet, int headStart, int headEnd) {
        Map<String, Integer> cellMap = new HashMap<>();

        // 获取所有表头行
        List<Row> headRows = new ArrayList<>();
        for (int i = headStart; i <= headEnd; i++) {
            headRows.add(sheet.getRow(i));
        }

        if (headRows.isEmpty() || headRows.get(0) == null) {
            return cellMap;
        }

        // 解析每一列的表头
        for (int col = 0; col < headRows.get(0).getPhysicalNumberOfCells(); col++) {
            List<String> columnHeaders = new ArrayList<>();

            for (Row headRow : headRows) {
                String cellValue = getCellValueAsString(sheet, headRow, col);
                if (StringUtils.isNotEmpty(cellValue) && !columnHeaders.contains(cellValue)) {
                    columnHeaders.add(cellValue);
                }
            }

            if (!columnHeaders.isEmpty()) {
                cellMap.put(StringUtils.join(columnHeaders, "-"), col);
            }
        }

        return cellMap;
    }

    /**
     * 获取单元格值（处理合并单元格）
     */
    private String getCellValueAsString(Sheet sheet, Row row, int column) {
        if (isMergedRegion(sheet, row.getRowNum(), column)) {
            return getMergedRegionValue(sheet, row.getRowNum(), column);
        }

        Object value = cellValueHandler.getCellValue(row, column);
        return value == null ? "" : value.toString().replace("\n", "");
    }

    /**
     * 判断是否为合并单元格
     */
    private boolean isMergedRegion(Sheet sheet, int row, int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();

        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (row >= range.getFirstRow() && row <= range.getLastRow() &&
                column >= range.getFirstColumn() && column <= range.getLastColumn()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取合并单元格的值
     */
    private String getMergedRegionValue(Sheet sheet, int row, int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();

        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (row >= range.getFirstRow() && row <= range.getLastRow() &&
                column >= range.getFirstColumn() && column <= range.getLastColumn()) {
                Row firstRow = sheet.getRow(range.getFirstRow());
                Object value = cellValueHandler.getCellValue(firstRow, range.getFirstColumn());
                return value == null ? "" : value.toString();
            }
        }

        return "";
    }

    /**
     * 构建字段映射（列索引 -> Field）
     */
    private Map<Integer, Field> buildFieldsMap(Map<String, Integer> cellMap) {
        Map<Integer, Field> fieldsMap = new HashMap<>();
        Field[] allFields = clazz.getDeclaredFields();

        for (Field field : allFields) {
            Excel attr = field.getAnnotation(Excel.class);
            if (attr != null && (attr.type() == Excel.Type.ALL || attr.type() == Excel.Type.IMPORT)) {
                field.setAccessible(true);
                Integer column = cellMap.get(attr.name());
                if (column != null) {
                    fieldsMap.put(column, field);
                }
            }
        }

        return fieldsMap;
    }

    /**
     * 读取行数据
     */
    private T readRowData(Row row, Map<Integer, Field> fieldsMap) throws Exception {
        if (row == null || fieldsMap.isEmpty()) {
            return null;
        }

        T entity = null;
        boolean hasData = false;

        for (Map.Entry<Integer, Field> entry : fieldsMap.entrySet()) {
            Object cellValue = cellValueHandler.getCellValue(row, entry.getKey());

            if (entity == null) {
                entity = clazz.newInstance();
            }

            Field field = entry.getValue();
            Excel attr = field.getAnnotation(Excel.class);

            // 转换字段值
            Object fieldValue = cellValueHandler.convertFieldValue(cellValue, field.getType(), attr);

            // 检查是否有数据
            if (fieldValue != null && StringUtils.isNotEmpty(fieldValue.toString())) {
                hasData = true;
            }

            // 处理读取转换表达式
            if (StringUtils.isNotEmpty(attr.readConverterExp()) && fieldValue != null) {
                fieldValue = cellValueHandler.reverseByExp(fieldValue.toString(), attr.readConverterExp(), attr.separator());
            }

            // 设置字段值
            if (fieldValue != null) {
                String propertyName = field.getName();
                if (StringUtils.isNotEmpty(attr.targetAttr())) {
                    propertyName = field.getName() + "." + attr.targetAttr();
                }
                ReflectUtils.invokeSetter(entity, propertyName, fieldValue);
            }
        }

        return hasData ? entity : null;
    }
}
