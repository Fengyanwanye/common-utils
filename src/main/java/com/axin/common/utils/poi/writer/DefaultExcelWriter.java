package com.axin.common.utils.poi.writer;

import com.axin.common.core.lang.UUID;
import com.axin.common.exception.CustomException;
import com.axin.common.utils.StringUtils;
import com.axin.common.utils.poi.handler.CellValueHandler;
import com.axin.common.utils.poi.style.ExcelStyleBuilder;
import com.axin.framework.aspectj.lang.annotation.Excel;
import com.axin.framework.config.CommonConfig;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认Excel写入器实现
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/24
 */
public class DefaultExcelWriter<T> implements ExcelWriter<T> {

    private static final Logger log = LoggerFactory.getLogger(DefaultExcelWriter.class);
    private static final int SHEET_SIZE = 65536;
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("######0.00");

    private final Class<T> clazz;
    private final CellValueHandler cellValueHandler;
    private List<FieldInfo> fieldInfos;
    private short maxRowHeight;

    public DefaultExcelWriter(Class<T> clazz) {
        this.clazz = clazz;
        this.cellValueHandler = new CellValueHandler();
        this.fieldInfos = extractFieldInfos();
        this.maxRowHeight = calculateMaxRowHeight();
    }

    @Override
    public String write(List<T> data, String sheetName) throws Exception {
        String filename = generateFilename(sheetName);
        String filePath = getAbsoluteFilePath(filename);

        try (FileOutputStream out = new FileOutputStream(filePath);
             Workbook workbook = createWorkbook()) {
            
            writeDataToWorkbook(workbook, data, sheetName, Excel.Type.EXPORT);
            workbook.write(out);
            
            return filename;
        } catch (Exception e) {
            log.error("导出Excel异常", e);
            throw new CustomException("导出Excel失败，请联系网站管理员！");
        }
    }

    @Override
    public void write(List<T> data, String sheetName, OutputStream outputStream) throws Exception {
        try (Workbook workbook = createWorkbook()) {
            writeDataToWorkbook(workbook, data, sheetName, Excel.Type.EXPORT);
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("导出Excel异常", e);
            throw new CustomException("导出Excel失败，请联系网站管理员！");
        }
    }

    @Override
    public void write(List<T> data, String sheetName, HttpServletResponse response) throws Exception {
        try (OutputStream out = response.getOutputStream()) {
            write(data, sheetName, out);
        } catch (Exception e) {
            log.error("导出Excel异常", e);
            throw new CustomException("导出Excel失败，请联系网站管理员！");
        }
    }

    @Override
    public String writeTemplate(String sheetName) throws Exception {
        return write(Collections.emptyList(), sheetName);
    }

    /**
     * 将数据写入Workbook
     */
    private void writeDataToWorkbook(Workbook workbook, List<T> data, String sheetName, Excel.Type type) {
        ExcelStyleBuilder styleBuilder = new ExcelStyleBuilder(workbook);
        
        // 计算需要多少个sheet
        int sheetCount = (int) Math.ceil((double) data.size() / SHEET_SIZE);
        sheetCount = Math.max(sheetCount, 1);

        for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
            Sheet sheet = createSheet(workbook, sheetName, sheetIndex, sheetCount);
            writeSheetData(sheet, styleBuilder, data, sheetIndex, type);
        }
    }

    /**
     * 创建Sheet
     */
    private Sheet createSheet(Workbook workbook, String sheetName, int index, int total) {
        Sheet sheet = workbook.createSheet();
        String name = total == 1 ? sheetName : sheetName + index;
        workbook.setSheetName(index, name);
        return sheet;
    }

    /**
     * 写入Sheet数据
     */
    private void writeSheetData(Sheet sheet, ExcelStyleBuilder styleBuilder, List<T> data, int sheetIndex, Excel.Type type) {
        // 创建表头
        Row headerRow = sheet.createRow(0);
        createHeader(sheet, headerRow, styleBuilder);

        // 如果是导出类型，填充数据
        if (type == Excel.Type.EXPORT && !data.isEmpty()) {
            fillData(sheet, styleBuilder, data, sheetIndex);
            addStatisticsRow(sheet, styleBuilder, data, sheetIndex);
        }
    }

    /**
     * 创建表头
     */
    private void createHeader(Sheet sheet, Row headerRow, ExcelStyleBuilder styleBuilder) {
        int column = 0;
        
        for (FieldInfo fieldInfo : fieldInfos) {
            Cell cell = headerRow.createCell(column);
            cell.setCellValue(fieldInfo.excel.name());
            cell.setCellStyle(styleBuilder.getHeaderStyle());
            
            // 设置列宽
            setColumnWidth(sheet, column, fieldInfo.excel);
            
            // 设置数据验证
            setDataValidation(sheet, column, fieldInfo.excel);
            
            column++;
        }
    }

    /**
     * 设置列宽
     */
    private void setColumnWidth(Sheet sheet, int column, Excel attr) {
        if (attr.name().contains("注：")) {
            sheet.setColumnWidth(column, 6000);
        } else {
            sheet.setColumnWidth(column, (int) ((attr.width() + 0.72) * 256));
        }
    }

    /**
     * 设置数据验证
     */
    private void setDataValidation(Sheet sheet, int column, Excel attr) {
        // 设置提示信息
        if (StringUtils.isNotEmpty(attr.prompt())) {
            setPrompt(sheet, "", attr.prompt(), 1, 100, column, column);
        }
        
        // 设置下拉选择
        if (attr.combo().length > 0) {
            setComboBox(sheet, attr.combo(), 1, 100, column, column);
        }
    }

    /**
     * 设置提示信息
     */
    private void setPrompt(Sheet sheet, String title, String content, int firstRow, int endRow, int firstCol, int endCol) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createCustomConstraint("DD1");
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        DataValidation validation = helper.createValidation(constraint, regions);
        validation.createPromptBox(title, content);
        validation.setShowPromptBox(true);
        sheet.addValidationData(validation);
    }

    /**
     * 设置下拉框
     */
    private void setComboBox(Sheet sheet, String[] options, int firstRow, int endRow, int firstCol, int endCol) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(options);
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        DataValidation validation = helper.createValidation(constraint, regions);
        
        if (validation instanceof XSSFDataValidation) {
            validation.setSuppressDropDownArrow(true);
            validation.setShowErrorBox(true);
        } else {
            validation.setSuppressDropDownArrow(false);
        }
        
        sheet.addValidationData(validation);
    }

    /**
     * 填充数据
     */
    private void fillData(Sheet sheet, ExcelStyleBuilder styleBuilder, List<T> data, int sheetIndex) {
        int startIndex = sheetIndex * SHEET_SIZE;
        int endIndex = Math.min(startIndex + SHEET_SIZE, data.size());

        for (int i = startIndex; i < endIndex; i++) {
            int rowNum = i - startIndex + 1;
            Row row = sheet.createRow(rowNum);
            row.setHeight(maxRowHeight);
            
            T entity = data.get(i);
            fillRowData(row, styleBuilder, entity);
        }
    }

    /**
     * 填充行数据
     */
    private void fillRowData(Row row, ExcelStyleBuilder styleBuilder, T entity) {
        int column = 0;
        
        for (FieldInfo fieldInfo : fieldInfos) {
            if (!fieldInfo.excel.isExport()) {
                column++;
                continue;
            }
            
            Cell cell = row.createCell(column);
            
            // 设置样式
            CellStyle style = getCellStyle(styleBuilder, fieldInfo.excel.align().value());
            cell.setCellStyle(style);
            
            // 获取字段值
            try {
                Object value = getFieldValue(entity, fieldInfo.field, fieldInfo.excel);
                
                // 格式化并设置值
                String formattedValue = cellValueHandler.formatCellValue(value, fieldInfo.excel);
                if (StringUtils.isNotEmpty(formattedValue) || value != null) {
                    cellValueHandler.setCellValue(cell, formattedValue.isEmpty() ? value : formattedValue, fieldInfo.excel);
                }
            } catch (Exception e) {
                log.error("设置单元格值失败", e);
            }
            
            column++;
        }
    }

    /**
     * 获取单元格样式
     */
    private CellStyle getCellStyle(ExcelStyleBuilder styleBuilder, int align) {
        switch (align) {
            case 1:
                return styleBuilder.getDataLeftStyle();
            case 2:
                return styleBuilder.getDataCenterStyle();
            case 3:
                return styleBuilder.getDataRightStyle();
            default:
                return styleBuilder.getDataStyle();
        }
    }

    /**
     * 获取字段值
     */
    private Object getFieldValue(T entity, Field field, Excel excel) throws Exception {
        field.setAccessible(true);
        Object value = field.get(entity);
        
        // 处理targetAttr
        if (StringUtils.isNotEmpty(excel.targetAttr()) && value != null) {
            String[] attrs = excel.targetAttr().split("\\.");
            for (String attr : attrs) {
                Field targetField = value.getClass().getDeclaredField(attr);
                targetField.setAccessible(true);
                value = targetField.get(value);
                if (value == null) {
                    break;
                }
            }
        }
        
        return value;
    }

    /**
     * 添加统计行
     */
    private void addStatisticsRow(Sheet sheet, ExcelStyleBuilder styleBuilder, List<T> data, int sheetIndex) {
        Map<Integer, Double> statistics = calculateStatistics(data, sheetIndex);
        
        if (statistics.isEmpty()) {
            return;
        }

        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        
        // 第一列显示"合计"
        Cell cell = row.createCell(0);
        cell.setCellStyle(styleBuilder.getTotalStyle());
        cell.setCellValue("合计");

        // 填充统计数据
        for (Map.Entry<Integer, Double> entry : statistics.entrySet()) {
            cell = row.createCell(entry.getKey());
            cell.setCellStyle(styleBuilder.getTotalStyle());
            cell.setCellValue(DOUBLE_FORMAT.format(entry.getValue()));
        }
    }

    /**
     * 计算统计数据
     */
    private Map<Integer, Double> calculateStatistics(List<T> data, int sheetIndex) {
        Map<Integer, Double> statistics = new HashMap<>();
        
        int startIndex = sheetIndex * SHEET_SIZE;
        int endIndex = Math.min(startIndex + SHEET_SIZE, data.size());

        for (int i = 0; i < fieldInfos.size(); i++) {
            FieldInfo fieldInfo = fieldInfos.get(i);
            
            if (!fieldInfo.excel.isStatistics()) {
                continue;
            }

            double sum = 0;
            for (int j = startIndex; j < endIndex; j++) {
                try {
                    Object value = getFieldValue(data.get(j), fieldInfo.field, fieldInfo.excel);
                    if (value != null) {
                        sum += Double.parseDouble(value.toString());
                    }
                } catch (Exception e) {
                    // 忽略非数值字段
                }
            }
            
            statistics.put(i, sum);
        }

        return statistics;
    }

    /**
     * 提取字段信息
     */
    private List<FieldInfo> extractFieldInfos() {
        List<FieldInfo> infos = new ArrayList<>();
        List<Field> allFields = new ArrayList<>();
        
        // 收集所有字段（包括父类）
        allFields.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
        allFields.addAll(Arrays.asList(clazz.getDeclaredFields()));

        for (Field field : allFields) {
            if (field.isAnnotationPresent(Excel.class)) {
                Excel excel = field.getAnnotation(Excel.class);
                infos.add(new FieldInfo(field, excel));
            }
        }

        // 按sort排序
        return infos.stream()
                .sorted(Comparator.comparing(info -> info.excel.sort()))
                .collect(Collectors.toList());
    }

    /**
     * 计算最大行高
     */
    private short calculateMaxRowHeight() {
        double maxHeight = 0;
        for (FieldInfo info : fieldInfos) {
            maxHeight = Math.max(maxHeight, info.excel.height());
        }
        return (short) (maxHeight * 20);
    }

    /**
     * 创建Workbook
     */
    private Workbook createWorkbook() {
        return new SXSSFWorkbook(500);
    }

    /**
     * 生成文件名
     */
    private String generateFilename(String sheetName) {
        return UUID.randomUUID().toString() + "_" + sheetName + ".xlsx";
    }

    /**
     * 获取绝对文件路径
     */
    private String getAbsoluteFilePath(String filename) {
        String downloadPath = CommonConfig.getDownloadPath() + filename;
        File file = new File(downloadPath);
        
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        
        return downloadPath;
    }

    /**
     * 字段信息内部类
     */
    private static class FieldInfo {
        Field field;
        Excel excel;

        FieldInfo(Field field, Excel excel) {
            this.field = field;
            this.excel = excel;
        }
    }
}
