package com.axin.common.utils.poi.validator;

import cn.hutool.core.collection.CollUtil;
import com.axin.common.exception.CustomException;
import com.axin.common.utils.StringUtils;
import com.axin.common.utils.poi.handler.CellValueHandler;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Excel验证工具类
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/24
 */
public class ExcelValidator {

    private static final String XLSX = ".xlsx";
    private static final String XLS = ".xls";
    
    private final CellValueHandler cellValueHandler = new CellValueHandler();

    /**
     * 校验上传的Excel文件
     *
     * @param file 上传文件
     */
    public static void validateUploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException("上传文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new CustomException("文件名为空");
        }

        boolean isExcelFile = StringUtils.endsWithIgnoreCase(originalFilename, XLS) ||
                              StringUtils.endsWithIgnoreCase(originalFilename, XLSX);
        
        if (!isExcelFile) {
            throw new CustomException("文件格式不正确，请上传Excel文件（.xls或.xlsx）");
        }
    }

    /**
     * 校验Excel表头是否与模板一致
     *
     * @param inputSheet   待校验的Sheet
     * @param headStart    表头起始行
     * @param headEnd      表头结束行
     * @param templateStream 模板文件输入流
     * @param sheetIndex   Sheet索引
     */
    public void validateHeader(Sheet inputSheet, int headStart, int headEnd, 
                               InputStream templateStream, Integer sheetIndex) throws IOException {
        if (inputSheet == null) {
            throw new CustomException("文件Sheet不存在");
        }

        // 获取模板Sheet
        Workbook templateWorkbook = WorkbookFactory.create(templateStream);
        Sheet templateSheet = sheetIndex != null ? 
            templateWorkbook.getSheetAt(sheetIndex) : templateWorkbook.getSheetAt(0);

        if (templateSheet == null) {
            throw new CustomException("模板文件Sheet不存在");
        }

        // 获取表头映射
        Map<Integer, String> inputHeaderMap = getHeaderMap(inputSheet, headStart, headEnd);
        Map<Integer, String> templateHeaderMap = getHeaderMap(templateSheet, headStart, headEnd);

        // 校验表头
        validateHeaderMap(inputHeaderMap, templateHeaderMap);
    }

    /**
     * 获取表头映射
     */
    public Map<Integer, String> getHeaderMap(Sheet sheet, int headStart, int headEnd) {
        Map<Integer, String> headerMap = new HashMap<>();

        // 获取所有表头行
        List<Row> headerRows = new ArrayList<>();
        for (int i = headStart; i <= headEnd; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                headerRows.add(row);
            }
        }

        if (CollUtil.isEmpty(headerRows)) {
            return headerMap;
        }

        // 解析每一列的表头
        for (int col = 0; col < headerRows.get(0).getLastCellNum(); col++) {
            List<String> columnHeaders = new ArrayList<>();
            
            for (Row row : headerRows) {
                String cellValue = getCellValueAsString(sheet, row, col);
                if (StringUtils.isNotEmpty(cellValue) && !columnHeaders.contains(cellValue)) {
                    columnHeaders.add(cellValue);
                }
            }

            if (CollUtil.isNotEmpty(columnHeaders)) {
                headerMap.put(col, StringUtils.join(columnHeaders, "-"));
            }
        }

        return headerMap;
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
     * 校验表头映射
     */
    private void validateHeaderMap(Map<Integer, String> inputMap, Map<Integer, String> templateMap) {
        Map<Integer, String> input = new HashMap<>(inputMap);
        Map<Integer, String> template = new HashMap<>(templateMap);

        // 确定需要遍历的Map
        Map<Integer, String> largerMap = input.size() > template.size() ? input : template;

        // 逐列校验
        for (Integer key : largerMap.keySet()) {
            String inputValue = input.get(key);
            String templateValue = template.get(key);

            // 移除null值列
            if (inputValue == null) {
                input.remove(key);
            }
            if (templateValue == null) {
                template.remove(key);
            }

            // 比较值
            if (!StringUtils.equals(inputValue, templateValue)) {
                throw new CustomException("模板不正确，请下载正确的模板重新上传！");
            }
        }

        // 最终大小校验
        if (input.size() != template.size()) {
            throw new CustomException("模板不正确，请下载正确的模板重新上传！");
        }
    }

    /**
     * 检查Sheet是否为空
     */
    public static boolean isSheetEmpty(Sheet sheet) {
        if (sheet == null) {
            return true;
        }
        
        int rows = sheet.getPhysicalNumberOfRows();
        return rows <= 1; // 只有表头或没有数据
    }

    /**
     * 获取Sheet的数据行数（不包括表头）
     */
    public static int getDataRowCount(Sheet sheet, int headerRows) {
        if (sheet == null) {
            return 0;
        }
        
        int totalRows = sheet.getPhysicalNumberOfRows();
        return Math.max(0, totalRows - headerRows);
    }
}
