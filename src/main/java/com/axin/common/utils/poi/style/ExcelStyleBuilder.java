package com.axin.common.utils.poi.style;

import org.apache.poi.ss.usermodel.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Excel样式构建器
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/24
 */
public class ExcelStyleBuilder {

    private final Workbook workbook;
    private final Map<String, CellStyle> styleCache = new HashMap<>();

    public ExcelStyleBuilder(Workbook workbook) {
        this.workbook = workbook;
    }

    /**
     * 获取表头样式
     */
    public CellStyle getHeaderStyle() {
        return styleCache.computeIfAbsent("header", key -> createHeaderStyle());
    }

    /**
     * 获取数据样式
     */
    public CellStyle getDataStyle() {
        return styleCache.computeIfAbsent("data", key -> createDataStyle());
    }

    /**
     * 获取左对齐数据样式
     */
    public CellStyle getDataLeftStyle() {
        return styleCache.computeIfAbsent("data1", key -> createDataStyle(HorizontalAlignment.LEFT));
    }

    /**
     * 获取居中数据样式
     */
    public CellStyle getDataCenterStyle() {
        return styleCache.computeIfAbsent("data2", key -> createDataStyle(HorizontalAlignment.CENTER));
    }

    /**
     * 获取右对齐数据样式
     */
    public CellStyle getDataRightStyle() {
        return styleCache.computeIfAbsent("data3", key -> createDataStyle(HorizontalAlignment.RIGHT));
    }

    /**
     * 获取合计样式
     */
    public CellStyle getTotalStyle() {
        return styleCache.computeIfAbsent("total", key -> createTotalStyle());
    }

    /**
     * 获取提醒样式（红色背景）
     */
    public CellStyle getNoticeStyle() {
        return styleCache.computeIfAbsent("notice", key -> createNoticeStyle());
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle() {
        CellStyle style = createBaseStyle();
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle() {
        return createDataStyle(HorizontalAlignment.CENTER);
    }

    /**
     * 创建指定对齐方式的数据样式
     */
    private CellStyle createDataStyle(HorizontalAlignment alignment) {
        CellStyle style = createBaseStyle();
        style.setAlignment(alignment);

        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        return style;
    }

    /**
     * 创建合计样式
     */
    private CellStyle createTotalStyle() {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        return style;
    }

    /**
     * 创建提醒样式
     */
    private CellStyle createNoticeStyle() {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 创建基础样式（带边框）
     */
    private CellStyle createBaseStyle() {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        return style;
    }

    /**
     * 清除样式缓存
     */
    public void clearCache() {
        styleCache.clear();
    }
}
