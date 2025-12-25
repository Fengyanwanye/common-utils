package com.axin.common.utils.poi.reader;

import com.axin.common.utils.StringUtils;
import com.axin.common.utils.poi.handler.CellValueHandler;
import com.axin.common.utils.reflect.ReflectUtils;
import com.axin.framework.aspectj.lang.annotation.Excel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 默认Excel读取器实现
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/24
 */
public class DefaultExcelReader<T> implements ExcelReader<T> {

    private final Class<T> clazz;
    private final CellValueHandler cellValueHandler;

    public DefaultExcelReader(Class<T> clazz) {
        this.clazz = clazz;
        this.cellValueHandler = new CellValueHandler();
    }

    @Override
    public List<T> read(InputStream inputStream) throws Exception {
        return read(inputStream, StringUtils.EMPTY, 0, 0, 1);
    }

    @Override
    public List<T> read(InputStream inputStream, String sheetName) throws Exception {
        return read(inputStream, sheetName, 0, 0, 1);
    }

    @Override
    public List<T> read(InputStream inputStream, String sheetName, int headStart, int headEnd, int dataStart) throws Exception {
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = getSheet(workbook, sheetName);

        if (sheet == null) {
            throw new IOException("文件sheet不存在");
        }

        return readDataFromSheet(sheet, headStart, headEnd, dataStart);
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
     * 从Sheet读取数据
     */
    private List<T> readDataFromSheet(Sheet sheet, int headStart, int headEnd, int dataStart) throws Exception {
        List<T> result = new ArrayList<>();

        // 获取列映射关系
        Map<String, Integer> cellMap = buildCellMap(sheet, headStart, headEnd);
        
        // 获取字段映射关系
        Map<Integer, Field> fieldsMap = buildFieldsMap(cellMap);

        int rows = sheet.getPhysicalNumberOfRows();
        for (int i = dataStart; i < rows; i++) {
            Row row = sheet.getRow(i);
            T entity = readRowData(row, fieldsMap);
            
            if (entity != null) {
                result.add(entity);
            }
        }

        return result;
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
