package com.axin.common.utils.poi.handler;

import com.axin.common.core.text.Convert;
import com.axin.common.utils.DateUtils;
import com.axin.common.utils.StringUtils;
import com.axin.common.utils.file.FileTypeUtils;
import com.axin.common.utils.file.ImageUtils;
import com.axin.framework.aspectj.lang.annotation.Excel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 单元格值处理器
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/24
 */
public class CellValueHandler {

    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("######0.00");

    /**
     * 从单元格读取值
     */
    public Object getCellValue(Row row, int column) {
        if (row == null) {
            return "";
        }

        Cell cell = row.getCell(column);
        if (cell == null) {
            return "";
        }

        try {
            CellType cellType = cell.getCellType();
            
            if (cellType == CellType.NUMERIC || cellType == CellType.FORMULA) {
                double numericValue = cell.getNumericCellValue();
                
                // 日期格式
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DateUtil.getJavaDate(numericValue);
                }
                
                // 整数或小数
                if (numericValue % 1 != 0) {
                    return new BigDecimal(String.valueOf(numericValue));
                } else {
                    return new DecimalFormat("0").format(numericValue);
                }
            } else if (cellType == CellType.STRING) {
                return cell.getStringCellValue();
            } else if (cellType == CellType.BOOLEAN) {
                return cell.getBooleanCellValue();
            } else if (cellType == CellType.ERROR) {
                return cell.getErrorCellValue();
            }
        } catch (Exception e) {
            return "";
        }

        return "";
    }

    /**
     * 设置单元格值
     */
    public void setCellValue(Cell cell, Object value, Excel attr) {
        if (attr.cellType() == Excel.ColumnType.STRING) {
            String strValue = StringUtils.isNull(value) ? attr.defaultValue() : value + attr.suffix();
            cell.setCellValue(strValue);
        } else if (attr.cellType() == Excel.ColumnType.NUMERIC) {
            String strValue = Convert.toStr(value);
            if (StringUtils.contains(strValue, ".")) {
                cell.setCellValue(Convert.toDouble(value));
            } else {
                cell.setCellValue(Convert.toInt(value));
            }
        } else if (attr.cellType() == Excel.ColumnType.IMAGE) {
            setImageValue(cell, value);
        }
    }

    /**
     * 设置图片值
     */
    private void setImageValue(Cell cell, Object value) {
        String imagePath = Convert.toStr(value);
        if (StringUtils.isEmpty(imagePath)) {
            return;
        }

        try {
            byte[] imageData = ImageUtils.getImage(imagePath);
            ClientAnchor anchor = new XSSFClientAnchor(
                0, 0, 0, 0,
                (short) cell.getColumnIndex(),
                cell.getRow().getRowNum(),
                (short) (cell.getColumnIndex() + 1),
                cell.getRow().getRowNum() + 1
            );

            Drawing<?> drawing = getDrawingPatriarch(cell.getSheet());
            drawing.createPicture(anchor, cell.getSheet().getWorkbook().addPicture(imageData, getImageType(imageData)));
        } catch (Exception e) {
            // 图片加载失败，忽略
        }
    }

    /**
     * 转换字段类型
     */
    public Object convertFieldValue(Object cellValue, Class<?> fieldType, Excel attr) {
        if (cellValue == null) {
            return null;
        }

        // 字符串类型
        if (String.class == fieldType) {
            String strValue = Convert.toStr(cellValue);
            if (StringUtils.endsWith(strValue, ".0")) {
                return StringUtils.substringBefore(strValue, ".0");
            }
            
            // 日期格式化
            if (StringUtils.isNotEmpty(attr.dateFormat()) && cellValue instanceof Date) {
                return DateUtils.parseDateToStr(attr.dateFormat(), (Date) cellValue);
            }
            
            return strValue;
        }
        
        // 数值类型
        if ((Integer.TYPE == fieldType || Integer.class == fieldType) && StringUtils.isNumeric(Convert.toStr(cellValue))) {
            return Convert.toInt(cellValue);
        }
        
        if (Long.TYPE == fieldType || Long.class == fieldType) {
            return Convert.toLong(cellValue);
        }
        
        if (Double.TYPE == fieldType || Double.class == fieldType) {
            return Convert.toDouble(cellValue);
        }
        
        if (Float.TYPE == fieldType || Float.class == fieldType) {
            return Convert.toFloat(cellValue);
        }
        
        if (BigDecimal.class == fieldType) {
            return Convert.toBigDecimal(cellValue);
        }
        
        // 日期类型
        if (Date.class == fieldType) {
            if (cellValue instanceof String) {
                return DateUtils.parseDate(cellValue);
            } else if (cellValue instanceof Double) {
                return DateUtil.getJavaDate((Double) cellValue);
            }
        }
        
        // 布尔类型
        if (Boolean.TYPE == fieldType || Boolean.class == fieldType) {
            return Convert.toBool(cellValue, false);
        }

        return cellValue;
    }

    /**
     * 格式化单元格显示值
     */
    public String formatCellValue(Object value, Excel attr) {
        if (value == null) {
            return "";
        }

        // 日期格式化
        if (StringUtils.isNotEmpty(attr.dateFormat()) && value instanceof Date) {
            return DateUtils.parseDateToStr(attr.dateFormat(), (Date) value);
        }
        if (StringUtils.isNotEmpty(attr.dateFormat()) && value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DateTimeFormatter.ofPattern(attr.dateFormat()));
        }

        // 读取转换表达式
        if (StringUtils.isNotEmpty(attr.readConverterExp())) {
            return convertByExp(Convert.toStr(value), attr.readConverterExp(), attr.separator());
        }

        // 字典类型
        if (StringUtils.isNotEmpty(attr.dictType())) {
            return convertDictByExp(Convert.toStr(value), attr.dictType(), attr.separator());
        }

        // BigDecimal精度处理
        if (value instanceof BigDecimal && attr.scale() != -1) {
            return ((BigDecimal) value).setScale(attr.scale(), attr.roundingMode()).toString();
        }

        return Convert.toStr(value);
    }

    /**
     * 解析导出值 0=男,1=女,2=未知
     */
    private String convertByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder result = new StringBuilder();
        String[] convertSource = converterExp.split(",");
        
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (itemArray.length < 2) {
                continue;
            }
            
            if (StringUtils.containsAny(separator, propertyValue)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[0].equals(value)) {
                        result.append(itemArray[1]).append(separator);
                        break;
                    }
                }
            } else {
                if (itemArray[0].equals(propertyValue)) {
                    return itemArray[1];
                }
            }
        }
        
        return StringUtils.stripEnd(result.toString(), separator);
    }

    /**
     * 反向解析值 男=0,女=1,未知=2
     */
    public String reverseByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder result = new StringBuilder();
        String[] convertSource = converterExp.split(",");
        
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (itemArray.length < 2) {
                continue;
            }
            
            if (StringUtils.containsAny(separator, propertyValue)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[1].equals(value)) {
                        result.append(itemArray[0]).append(separator);
                        break;
                    }
                }
            } else {
                if (itemArray[1].equals(propertyValue)) {
                    return itemArray[0];
                }
            }
        }
        
        return StringUtils.stripEnd(result.toString(), separator);
    }

    /**
     * 解析字典值（预留接口）
     */
    private String convertDictByExp(String dictValue, String dictType, String separator) {
        // 可以接入字典服务
        return "";
    }

    /**
     * 获取画布
     */
    private Drawing<?> getDrawingPatriarch(Sheet sheet) {
        if (sheet.getDrawingPatriarch() == null) {
            sheet.createDrawingPatriarch();
        }
        return sheet.getDrawingPatriarch();
    }

    /**
     * 获取图片类型
     */
    private int getImageType(byte[] imageData) {
        String type = FileTypeUtils.getFileExtendName(imageData);
        if ("JPG".equalsIgnoreCase(type)) {
            return Workbook.PICTURE_TYPE_JPEG;
        } else if ("PNG".equalsIgnoreCase(type)) {
            return Workbook.PICTURE_TYPE_PNG;
        }
        return Workbook.PICTURE_TYPE_JPEG;
    }
}
