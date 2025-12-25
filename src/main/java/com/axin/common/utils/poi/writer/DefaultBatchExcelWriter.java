package com.axin.common.utils.poi.writer;

import com.axin.common.core.lang.UUID;
import com.axin.common.exception.CustomException;
import com.axin.common.utils.poi.handler.CellValueHandler;
import com.axin.common.utils.poi.style.ExcelStyleBuilder;
import com.axin.framework.aspectj.lang.annotation.Excel;
import com.axin.framework.config.CommonConfig;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认分批Excel写入器实现
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/24
 */
public class DefaultBatchExcelWriter<T> implements BatchExcelWriter<T> {

    private static final Logger log = LoggerFactory.getLogger(DefaultBatchExcelWriter.class);
    private static final int SHEET_SIZE = 65536;

    private final Class<T> clazz;
    private final String sheetName;
    private final CellValueHandler cellValueHandler;
    private final Workbook workbook;
    private final ExcelStyleBuilder styleBuilder;
    private final List<FieldInfo> fieldInfos;
    private final short maxRowHeight;

    private Sheet currentSheet;
    private int currentSheetIndex = 0;
    private int currentRowNum = 0;
    private int totalRowsWritten = 0;
    private boolean headerWritten = false;
    private boolean closed = false;

    public DefaultBatchExcelWriter(Class<T> clazz, String sheetName) {
        this.clazz = clazz;
        this.sheetName = sheetName;
        this.cellValueHandler = new CellValueHandler();
        this.workbook = new SXSSFWorkbook(500);
        this.styleBuilder = new ExcelStyleBuilder(workbook);
        this.fieldInfos = extractFieldInfos();
        this.maxRowHeight = calculateMaxRowHeight();
        
        // 创建第一个Sheet
        createNewSheet();
    }

    @Override
    public void writeBatch(List<T> batch) throws Exception {
        if (closed) {
            throw new IllegalStateException("Writer已关闭，无法继续写入");
        }

        if (batch == null || batch.isEmpty()) {
            return;
        }

        for (T entity : batch) {
            // 检查是否需要创建新的Sheet
            if (currentRowNum >= SHEET_SIZE) {
                currentSheetIndex++;
                createNewSheet();
            }

            // 第一行写表头
            if (!headerWritten) {
                writeHeader();
                headerWritten = true;
            }

            // 写入数据行
            writeDataRow(entity);
            currentRowNum++;
            totalRowsWritten++;
        }
    }

    @Override
    public String finish() throws Exception {
        if (closed) {
            throw new IllegalStateException("Writer已关闭");
        }

        String filename = generateFilename(sheetName);
        String filePath = getAbsoluteFilePath(filename);

        try (FileOutputStream out = new FileOutputStream(filePath)) {
            workbook.write(out);
            return filename;
        } catch (Exception e) {
            log.error("保存Excel文件失败", e);
            throw new CustomException("保存Excel文件失败");
        } finally {
            close();
        }
    }

    @Override
    public void finish(OutputStream outputStream) throws Exception {
        if (closed) {
            throw new IllegalStateException("Writer已关闭");
        }

        try {
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("输出Excel失败", e);
            throw new CustomException("输出Excel失败");
        } finally {
            close();
        }
    }

    @Override
    public int getWrittenRows() {
        return totalRowsWritten;
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            if (workbook != null) {
                workbook.close();
            }
            closed = true;
        }
    }

    /**
     * 创建新的Sheet
     */
    private void createNewSheet() {
        currentSheet = workbook.createSheet();
        String name = currentSheetIndex == 0 ? sheetName : sheetName + currentSheetIndex;
        workbook.setSheetName(currentSheetIndex, name);
        currentRowNum = 0;
        headerWritten = false;
    }

    /**
     * 写入表头
     */
    private void writeHeader() {
        Row headerRow = currentSheet.createRow(currentRowNum);
        int column = 0;

        for (FieldInfo fieldInfo : fieldInfos) {
            Cell cell = headerRow.createCell(column);
            cell.setCellValue(fieldInfo.excel.name());
            cell.setCellStyle(styleBuilder.getHeaderStyle());

            // 设置列宽
            setColumnWidth(currentSheet, column, fieldInfo.excel);

            // 设置数据验证
            setDataValidation(currentSheet, column, fieldInfo.excel);

            column++;
        }

        currentRowNum++;
    }

    /**
     * 写入数据行
     */
    private void writeDataRow(T entity) throws Exception {
        Row row = currentSheet.createRow(currentRowNum);
        row.setHeight(maxRowHeight);

        int column = 0;
        for (FieldInfo fieldInfo : fieldInfos) {
            if (!fieldInfo.excel.isExport()) {
                column++;
                continue;
            }

            Cell cell = row.createCell(column);

            // 设置样式
            CellStyle style = getCellStyle(fieldInfo.excel.align().value());
            cell.setCellStyle(style);

            // 获取字段值并设置
            try {
                Object value = getFieldValue(entity, fieldInfo.field, fieldInfo.excel);
                String formattedValue = cellValueHandler.formatCellValue(value, fieldInfo.excel);
                
                if (!formattedValue.isEmpty() || value != null) {
                    cellValueHandler.setCellValue(cell, formattedValue.isEmpty() ? value : formattedValue, fieldInfo.excel);
                }
            } catch (Exception e) {
                log.error("设置单元格值失败", e);
            }

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
        if (attr.prompt() != null && !attr.prompt().isEmpty()) {
            setPrompt(sheet, "", attr.prompt(), 1, SHEET_SIZE - 1, column, column);
        }

        if (attr.combo().length > 0) {
            setComboBox(sheet, attr.combo(), 1, SHEET_SIZE - 1, column, column);
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
     * 获取单元格样式
     */
    private CellStyle getCellStyle(int align) {
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

        if (excel.targetAttr() != null && !excel.targetAttr().isEmpty() && value != null) {
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
     * 提取字段信息
     */
    private List<FieldInfo> extractFieldInfos() {
        List<FieldInfo> infos = new ArrayList<>();
        List<Field> allFields = new ArrayList<>();

        allFields.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
        allFields.addAll(Arrays.asList(clazz.getDeclaredFields()));

        for (Field field : allFields) {
            if (field.isAnnotationPresent(Excel.class)) {
                Excel excel = field.getAnnotation(Excel.class);
                infos.add(new FieldInfo(field, excel));
            }
        }

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
