package com.axin.common.utils.poi;

import cn.hutool.core.collection.CollUtil;
import com.axin.common.core.text.Convert;
import com.axin.common.exception.CustomException;
import com.axin.common.utils.DateUtils;
import com.axin.common.utils.StringUtils;
import com.axin.common.utils.file.FileTypeUtils;
import com.axin.common.utils.file.FileUtils;
import com.axin.common.utils.file.ImageUtils;
import com.axin.common.utils.reflect.ReflectUtils;
import com.axin.framework.aspectj.lang.annotation.Excel;
import com.axin.framework.aspectj.lang.annotation.Excels;
import com.axin.framework.config.CommonConfig;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Maps;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Excel工具类（已废弃）
 * <p>
 * 该类已被重构，不再推荐使用。请改用 {@link ExcelFacade} 获得更好的API体验。
 * <p>
 * 迁移指南：
 * <pre>
 * // 旧方式（不推荐）
 * ExcelUtils<User> util = new ExcelUtils<>(User.class);
 * util.init(userList, "用户列表", Excel.Type.EXPORT);
 * String filename = util.exportExcel();
 * 
 * // 新方式（推荐）
 * String filename = ExcelFacade.exportExcel(userList, "用户列表", User.class);
 * </pre>
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/23 10:56
 * @deprecated 已废弃，请使用 {@link ExcelFacade} 替代
 * @see ExcelFacade
 */
@Deprecated
public class ExcelUtils<T> {
    private static final Logger log = LoggerFactory.getLogger(ExcelUtils.class);

    public static final String XLSX = ".xlsx";
    public static final String XLS = ".xls";
    /**
     * Excel sheet最大行数，默认65536
     */
    public static final int sheetSize = 65536;

    /**
     * 工作表名称
     */
    private String sheetName;

    /**
     * 导出类型（EXPORT:导出数据；IMPORT：导入模板）
     */
    private Excel.Type type;

    /**
     * 工作薄对象
     */
    private Workbook wb;

    /**
     * 工作表对象
     */
    private Sheet sheet;

    /**
     * 样式列表
     */
    private Map<String, CellStyle> styles;

    /**
     * 导入导出数据列表
     */
    private List<T> list;

    /**
     * 注解列表
     */
    private List<Object[]> fields;

    /**
     * 最大高度
     */
    private short maxHeight;

    /**
     * 统计列表
     */
    private Map<Integer, Double> statistics = new HashMap<Integer, Double>();

    /**
     * 数字格式
     */
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("######0.00");

    /**
     * 实体对象
     */
    public Class<T> clazz;

    public Sheet getSheet() {
        return sheet;
    }

    public ExcelUtils(Class<T> clazz) {
        this.clazz = clazz;
    }

    public ExcelUtils(InputStream inputStream) throws FileNotFoundException, IOException {
        this.wb = new XSSFWorkbook(inputStream);
        this.sheet = this.wb.getSheetAt(0);
    }

    public void init(List<T> list, String sheetName, Excel.Type type) {
        if (list == null) {
            list = new ArrayList<T>();
        }
        this.list = list;
        this.sheetName = sheetName;
        this.type = type;
        createExcelField();
        createWorkbook();
    }

    /**
     * 对excel表单默认第一个索引名转换成list
     *
     * @param is 输入流
     * @return 转换后集合
     */
    public List<T> importExcel(InputStream is) throws Exception {
        return importExcel(StringUtils.EMPTY, is, 0, 0);
    }

    /**
     * 对excel表单指定表格索引名转换成list
     *
     * @param sheetName 表格索引名
     * @param is        输入流
     * @return 转换后集合
     */
    public List<T> importExcel(String sheetName, InputStream is, int headStart, int headEnd) throws Exception {
        return importExcel(sheetName, is, headStart, headEnd, headEnd + 1);
    }

    /**
     * 对excel表单指定表格索引名转换成list
     *
     * @param sheetName 表格索引名
     * @param is        输入流
     * @return 转换后集合
     */
    public List<T> importExcel(String sheetName, InputStream is, int headStart, int headEnd, int dataStart) throws Exception {

        this.type = Excel.Type.IMPORT;
        this.wb = WorkbookFactory.create(is);
        List<T> list = new ArrayList<T>();
        if (StringUtils.isNotEmpty(sheetName)) {
            // 如果指定sheet名,则取指定sheet中的内容.
            this.sheet = wb.getSheet(sheetName);
        } else {
            // 如果传入的sheet名不存在则默认指向第1个sheet.
            this.sheet = wb.getSheetAt(0);
        }

        if (sheet == null) {
            throw new IOException("文件sheet不存在");
        }

        Map<String, Integer> cellMap = getCellMap(sheet, headStart, headEnd);

        int rows = sheet.getPhysicalNumberOfRows();

        if (rows > 0) {

            // 有数据时才处理 得到类的所有field.
            Field[] allFields = clazz.getDeclaredFields();
            // 定义一个map用于存放列的序号和field.
            Map<Integer, Field> fieldsMap = new HashMap<Integer, Field>();
            for (int col = 0; col < allFields.length; col++) {
                Field field = allFields[col];
                Excel attr = field.getAnnotation(Excel.class);
                if (attr != null && (attr.type() == Excel.Type.ALL || attr.type() == type)) {
                    // 设置类的私有字段属性可访问.
                    field.setAccessible(true);
                    Integer column = cellMap.get(attr.name());
                    if (column != null) {
                        fieldsMap.put(column, field);
                    }
                }
            }
            for (int i = dataStart; i < rows; i++) {
                // 从第dataStart行开始取数据,默认第一行是表头.
                Row row = sheet.getRow(i);
                T entity = null;
                //判断是否空行
                boolean isExport = false;
                for (Map.Entry<Integer, Field> entry : fieldsMap.entrySet()) {

                    Object val = this.getCellValue(row, entry.getKey());

                    // 如果不存在实例则新建.
                    entity = (entity == null ? clazz.newInstance() : entity);
                    // 从map中得到对应列的field.
                    Field field = fieldsMap.get(entry.getKey());
                    // 取得类型,并根据对象类型设置值.
                    Class<?> fieldType = field.getType();
                    if (String.class == fieldType) {
                        String s = Convert.toStr(val);
                        if (StringUtils.isNotEmpty(s) && !isExport) {
                            isExport = true;
                        }
                        if (StringUtils.endsWith(s, ".0")) {
                            val = StringUtils.substringBefore(s, ".0");
                        } else {
                            String dateFormat = field.getAnnotation(Excel.class).dateFormat();
                            if (StringUtils.isNotEmpty(dateFormat)) {
                                val = DateUtils.parseDateToStr(dateFormat, (Date) val);
                            } else {
                                val = Convert.toStr(val);
                            }
                        }
                    } else if ((Integer.TYPE == fieldType || Integer.class == fieldType) && StringUtils.isNumeric(Convert.toStr(val))) {
                        val = Convert.toInt(val);
                    } else if (Long.TYPE == fieldType || Long.class == fieldType) {
                        val = Convert.toLong(val);
                    } else if (Double.TYPE == fieldType || Double.class == fieldType) {
                        val = Convert.toDouble(val);
                    } else if (Float.TYPE == fieldType || Float.class == fieldType) {
                        val = Convert.toFloat(val);
                    } else if (BigDecimal.class == fieldType) {
                        val = Convert.toBigDecimal(val);
                    } else if (Date.class == fieldType) {
                        if (val instanceof String) {
                            val = DateUtils.parseDate(val);
                        } else if (val instanceof Double) {
                            val = DateUtil.getJavaDate((Double) val);
                        }
                    } else if (Boolean.TYPE == fieldType || Boolean.class == fieldType) {
                        val = Convert.toBool(val, false);
                    }
                    if (StringUtils.isNotNull(fieldType)) {
                        Excel attr = field.getAnnotation(Excel.class);
                        String propertyName = field.getName();
                        if (StringUtils.isNotEmpty(attr.targetAttr())) {
                            propertyName = field.getName() + "." + attr.targetAttr();
                        } else if (StringUtils.isNotEmpty(attr.readConverterExp())) {
                            val = reverseByExp(Convert.toStr(val), attr.readConverterExp(), attr.separator());
                        } else if (StringUtils.isNotEmpty(attr.dictType())) {
                            val = reverseDictByExp(Convert.toStr(val), attr.dictType(), attr.separator());
                        }
                        ReflectUtils.invokeSetter(entity, propertyName, val);
                    }
                }
                if (isExport) {
                    list.add(entity);
                }
            }
        }
        this.list = list;
        return list;
    }

    private Map<String, Integer> getCellMap(Sheet sheet, int start, int end) {
        // 定义一个map用于存放excel列的序号和field.
        Map<String, Integer> cellMap = new HashMap<String, Integer>();
        // 获取表头
        List<Row> headList = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            headList.add(sheet.getRow(i));
        }

        for (int i = 0; i < headList.get(0).getPhysicalNumberOfCells(); i++) {
            Cell cell = headList.get(0).getCell(i);
            if (StringUtils.isNotNull(cell)) {
                List<String> value = new ArrayList<>();
                for (Row cells : headList) {
                    String cellValue;
                    boolean isMerge = isMergedRegion(sheet, cells.getRowNum(), i);
                    //判断是否具有合并单元格
                    if (isMerge) {
                        cellValue = getMergedRegionValue(sheet, cells.getRowNum(), i);
                    } else {
                        //20210926 去除\n
                        cellValue = this.getCellValue(cells, i).toString().replace("\n", "");
                    }
                    if (StringUtils.isNotEmpty(cellValue) && !value.contains(cellValue)) {
                        value.add(cellValue);
                    }
                }
                cellMap.put(StringUtils.join(value, "-"), i);
            } else {
                cellMap.put(null, i);
            }
        }
        return cellMap;
    }

    /**
     * 判断指定的单元格是否是合并单元格
     *
     * @param sheet
     * @param row    行下标
     * @param column 列下标
     * @return
     */
    @SuppressWarnings("unused")
    public static boolean isMergedRegion(Sheet sheet, int row, int column) {

        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {

            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            if (row >= firstRow && row <= lastRow) {
                if (column >= firstColumn && column <= lastColumn) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取合并单元格的值
     *
     * @param sheet
     * @param row
     * @param column
     * @return
     */
    private String getMergedRegionValue(Sheet sheet, int row, int column) {

        int sheetMergeCount = sheet.getNumMergedRegions();

        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress ca = sheet.getMergedRegion(i);
            int firstColumn = ca.getFirstColumn();
            int lastColumn = ca.getLastColumn();
            int firstRow = ca.getFirstRow();
            int lastRow = ca.getLastRow();
            if (row >= firstRow && row <= lastRow) {
                if (column >= firstColumn && column <= lastColumn) {
                    Row fRow = sheet.getRow(firstRow);
                    Cell fCell = fRow.getCell(firstColumn);
                    return getCellValue(fRow, firstColumn).toString();
                }
            }
        }

        return null;
    }

    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @param list      导出数据集合
     * @param sheetName 工作表的名称
     * @return 结果
     */
    public String exportExcel(List<T> list, String sheetName) {
        this.init(list, sheetName, Excel.Type.EXPORT);
        return exportExcel();
    }

    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @param sheetName 工作表的名称
     * @return 结果
     */
    public String importTemplateExcel(String sheetName) {
        this.init(null, sheetName, Excel.Type.IMPORT);
        return exportExcel();
    }

    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @return 结果
     */
    public String exportExcel() {
        OutputStream out = null;
        try {
            // 取出一共有多少个sheet.
            double sheetNo = Math.ceil(list.size() / sheetSize);
            for (int index = 0; index <= sheetNo; index++) {
                createSheet(sheetNo, index);

                // 产生一行
                Row row = sheet.createRow(0);
                int column = 0;
                // 写入各个字段的列头名称
                for (Object[] os : fields) {
                    Excel excel = (Excel) os[1];
                    this.createCell(excel, row, column++);
                }
                if (Excel.Type.EXPORT.equals(type)) {
                    fillExcelData(index, row);
                    addStatisticsRow();
                }
            }
            String filename = encodingFilename(sheetName);
            out = new FileOutputStream(getAbsoluteFile(filename));
            wb.write(out);
            return filename;
        } catch (Exception e) {
            log.error("导出Excel异常{}", e.getMessage());
            throw new CustomException("导出Excel失败，请联系网站管理员！");
        } finally {
            if (wb != null) {
                try {
                    wb.close();
                } catch (IOException e1) {
                    log.error("", e1);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    log.error("", e1);
                }
            }
        }
    }

    /**
     * 填充excel数据
     *
     * @param index 序号
     * @param row   单元格行
     */
    public void fillExcelData(int index, Row row) {
        int startNo = index * sheetSize;
        int endNo = Math.min(startNo + sheetSize, list.size());
        for (int i = startNo; i < endNo; i++) {
            row = sheet.createRow(i + 1 - startNo);
            // 得到导出对象.
            T vo = (T) list.get(i);
            int column = 0;
            for (Object[] os : fields) {
                Field field = (Field) os[0];
                Excel excel = (Excel) os[1];
                // 设置实体类私有属性可访问
                field.setAccessible(true);
                this.addCell(excel, row, vo, field, column++);
            }
        }
    }

    /**
     * 创建表格样式
     *
     * @param wb 工作薄对象
     * @return 样式列表
     */
    private Map<String, CellStyle> createStyles(Workbook wb) {
        // 写入各条记录,每条记录对应excel表中的一行
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        CellStyle style = wb.createCellStyle();
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
        Font dataFont = wb.createFont();
        dataFont.setFontName("Arial");
        dataFont.setFontHeightInPoints((short) 10);
        style.setFont(dataFont);
        styles.put("data", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = wb.createFont();
        headerFont.setFontName("Arial");
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(headerFont);
        styles.put("header", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font totalFont = wb.createFont();
        totalFont.setFontName("Arial");
        totalFont.setFontHeightInPoints((short) 10);
        style.setFont(totalFont);
        styles.put("total", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        style.setAlignment(HorizontalAlignment.LEFT);
        styles.put("data1", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        style.setAlignment(HorizontalAlignment.CENTER);
        styles.put("data2", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        styles.put("data3", style);

        return styles;
    }

    /**
     * 创建单元格
     */
    public Cell createCell(Excel attr, Row row, int column) {
        // 创建列
        Cell cell = row.createCell(column);
        // 写入列信息
        cell.setCellValue(attr.name());
        setDataValidation(attr, row, column);
        cell.setCellStyle(styles.get("header"));
        return cell;
    }

    /**
     * 设置单元格信息
     *
     * @param value 单元格值
     * @param attr  注解相关
     * @param cell  单元格信息
     */
    public void setCellVo(Object value, Excel attr, Cell cell) {
        if (Excel.ColumnType.STRING == attr.cellType()) {
            cell.setCellValue(StringUtils.isNull(value) ? attr.defaultValue() : value + attr.suffix());
        } else if (Excel.ColumnType.NUMERIC == attr.cellType()) {
            cell.setCellValue(StringUtils.contains(Convert.toStr(value), ".") ? Convert.toDouble(value) : Convert.toInt(value));
        } else if (Excel.ColumnType.IMAGE == attr.cellType()) {
            ClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, (short) cell.getColumnIndex(), cell.getRow().getRowNum(), (short) (cell.getColumnIndex() + 1),
                    cell.getRow().getRowNum() + 1);
            String imagePath = Convert.toStr(value);
            if (StringUtils.isNotEmpty(imagePath)) {
                byte[] data = ImageUtils.getImage(imagePath);
                getDrawingPatriarch(cell.getSheet()).createPicture(anchor,
                        cell.getSheet().getWorkbook().addPicture(data, getImageType(data)));
            }
        }
    }

    /**
     * 获取画布
     */
    public static Drawing<?> getDrawingPatriarch(Sheet sheet) {
        if (sheet.getDrawingPatriarch() == null) {
            sheet.createDrawingPatriarch();
        }
        return sheet.getDrawingPatriarch();
    }

    /**
     * 获取图片类型,设置图片插入类型
     */
    public int getImageType(byte[] value) {
        String type = FileTypeUtils.getFileExtendName(value);
        if ("JPG".equalsIgnoreCase(type)) {
            return Workbook.PICTURE_TYPE_JPEG;
        } else if ("PNG".equalsIgnoreCase(type)) {
            return Workbook.PICTURE_TYPE_PNG;
        }
        return Workbook.PICTURE_TYPE_JPEG;
    }

    /**
     * 创建表格样式
     */
    public void setDataValidation(Excel attr, Row row, int column) {
        if (attr.name().indexOf("注：") >= 0) {
            sheet.setColumnWidth(column, 6000);
        } else {
            // 设置列宽
            sheet.setColumnWidth(column, (int) ((attr.width() + 0.72) * 256));
        }
        // 如果设置了提示信息则鼠标放上去提示.
        if (StringUtils.isNotEmpty(attr.prompt())) {
            // 这里默认设了2-101列提示.
            setXSSFPrompt(sheet, "", attr.prompt(), 1, 100, column, column);
        }
        // 如果设置了combo属性则本列只能选择不能输入
        if (attr.combo().length > 0) {
            // 这里默认设了2-101列只能选择不能输入.
            setXSSFValidation(sheet, attr.combo(), 1, 100, column, column);
        }
    }

    /**
     * 添加单元格
     */
    public Cell addCell(Excel attr, Row row, T vo, Field field, int column) {
        Cell cell = null;
        try {
            // 设置行高
            row.setHeight(maxHeight);
            // 根据Excel中设置情况决定是否导出,有些情况需要保持为空,希望用户填写这一列.
            if (attr.isExport()) {
                // 创建cell
                cell = row.createCell(column);
                int align = attr.align().value();
                cell.setCellStyle(styles.get("data" + (align >= 1 && align <= 3 ? align : "")));

                // 用于读取对象中的属性
                Object value = getTargetValue(vo, field, attr);
                String dateFormat = attr.dateFormat();
                String readConverterExp = attr.readConverterExp();
                String separator = attr.separator();
                String dictType = attr.dictType();
                if (StringUtils.isNotEmpty(dateFormat) && StringUtils.isNotNull(value)) {
                    cell.setCellValue(DateUtils.parseDateToStr(dateFormat, (Date) value));
                } else if (StringUtils.isNotEmpty(readConverterExp) && StringUtils.isNotNull(value)) {
                    cell.setCellValue(convertByExp(Convert.toStr(value), readConverterExp, separator));
                } else if (StringUtils.isNotEmpty(dictType) && StringUtils.isNotNull(value)) {
                    cell.setCellValue(convertDictByExp(Convert.toStr(value), dictType, separator));
                } else if (value instanceof BigDecimal && -1 != attr.scale()) {
                    cell.setCellValue((((BigDecimal) value).setScale(attr.scale(), attr.roundingMode())).toString());
                } else {
                    // 设置列类型
                    setCellVo(value, attr, cell);
                }
                addStatisticsData(column, Convert.toStr(value), attr);
            }
        } catch (Exception e) {
            log.error("导出Excel失败{}", e);
        }
        return cell;
    }

    /**
     * 设置 POI XSSFSheet 单元格提示
     *
     * @param sheet         表单
     * @param promptTitle   提示标题
     * @param promptContent 提示内容
     * @param firstRow      开始行
     * @param endRow        结束行
     * @param firstCol      开始列
     * @param endCol        结束列
     */
    public void setXSSFPrompt(Sheet sheet, String promptTitle, String promptContent, int firstRow, int endRow,
                              int firstCol, int endCol) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createCustomConstraint("DD1");
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        DataValidation dataValidation = helper.createValidation(constraint, regions);
        dataValidation.createPromptBox(promptTitle, promptContent);
        dataValidation.setShowPromptBox(true);
        sheet.addValidationData(dataValidation);
    }

    /**
     * 设置某些列的值只能输入预制的数据,显示下拉框.
     *
     * @param sheet    要设置的sheet.
     * @param textlist 下拉框显示的内容
     * @param firstRow 开始行
     * @param endRow   结束行
     * @param firstCol 开始列
     * @param endCol   结束列
     * @return 设置好的sheet.
     */
    public void setXSSFValidation(Sheet sheet, String[] textlist, int firstRow, int endRow, int firstCol, int endCol) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        // 加载下拉列表内容
        DataValidationConstraint constraint = helper.createExplicitListConstraint(textlist);
        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        // 数据有效性对象
        DataValidation dataValidation = helper.createValidation(constraint, regions);
        // 处理Excel兼容性问题
        if (dataValidation instanceof XSSFDataValidation) {
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.setShowErrorBox(true);
        } else {
            dataValidation.setSuppressDropDownArrow(false);
        }

        sheet.addValidationData(dataValidation);
    }

    /**
     * 解析导出值 0=男,1=女,2=未知
     *
     * @param propertyValue 参数值
     * @param converterExp  翻译注解
     * @param separator     分隔符
     * @return 解析后值
     */
    public static String convertByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = converterExp.split(",");
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StringUtils.containsAny(separator, propertyValue)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[0].equals(value)) {
                        propertyString.append(itemArray[1] + separator);
                        break;
                    }
                }
            } else {
                if (itemArray[0].equals(propertyValue)) {
                    return itemArray[1];
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 反向解析值 男=0,女=1,未知=2
     *
     * @param propertyValue 参数值
     * @param converterExp  翻译注解
     * @param separator     分隔符
     * @return 解析后值
     */
    public static String reverseByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = converterExp.split(",");
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StringUtils.containsAny(separator, propertyValue)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[1].equals(value)) {
                        propertyString.append(itemArray[0] + separator);
                        break;
                    }
                }
            } else {
                if (itemArray[1].equals(propertyValue)) {
                    return itemArray[0];
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 解析字典值
     *
     * @param dictValue 字典值
     * @param dictType  字典类型
     * @param separator 分隔符
     * @return 字典标签
     */
    public static String convertDictByExp(String dictValue, String dictType, String separator) {
        return "";
//        return DictUtils.getDictLabel(dictType, dictValue, separator);
    }

    /**
     * 反向解析值字典值
     *
     * @param dictLabel 字典标签
     * @param dictType  字典类型
     * @param separator 分隔符
     * @return 字典值
     */
    public static String reverseDictByExp(String dictLabel, String dictType, String separator) {
        return "";
    }

    /**
     * 合计统计信息
     */
    private void addStatisticsData(Integer index, String text, Excel entity) {
        if (entity != null && entity.isStatistics()) {
            Double temp = 0D;
            if (!statistics.containsKey(index)) {
                statistics.put(index, temp);
            }
            try {
                temp = Double.valueOf(text);
            } catch (NumberFormatException e) {
            }
            statistics.put(index, statistics.get(index) + temp);
        }
    }

    /**
     * 创建统计行
     */
    public void addStatisticsRow() {
        if (statistics.size() > 0) {
            Cell cell = null;
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            Set<Integer> keys = statistics.keySet();
            cell = row.createCell(0);
            cell.setCellStyle(styles.get("total"));
            cell.setCellValue("合计");

            for (Integer key : keys) {
                cell = row.createCell(key);
                cell.setCellStyle(styles.get("total"));
                cell.setCellValue(DOUBLE_FORMAT.format(statistics.get(key)));
            }
            statistics.clear();
        }
    }

    /**
     * 编码文件名
     */
    public String encodingFilename(String filename) {
        filename = UUID.randomUUID().toString() + "_" + filename + ".xlsx";
        return filename;
    }

    /**
     * 获取下载路径
     *
     * @param filename 文件名称
     */
    public String getAbsoluteFile(String filename) {
        String downloadPath = CommonConfig.getDownloadPath() + filename;
        File desc = new File(downloadPath);
        if (!desc.getParentFile().exists()) {
            desc.getParentFile().mkdirs();
        }
        return downloadPath;
    }

    /**
     * 获取bean中的属性值
     *
     * @param vo    实体对象
     * @param field 字段
     * @param excel 注解
     * @return 最终的属性值
     * @throws Exception
     */
    private Object getTargetValue(T vo, Field field, Excel excel) throws Exception {
        Object o = field.get(vo);
        if (StringUtils.isNotEmpty(excel.targetAttr())) {
            String target = excel.targetAttr();
            if (target.indexOf(".") > -1) {
                String[] targets = target.split("[.]");
                for (String name : targets) {
                    o = getValue(o, name);
                }
            } else {
                o = getValue(o, target);
            }
        }
        return o;
    }

    /**
     * 以类的属性的get方法方法形式获取值
     *
     * @param o
     * @param name
     * @return value
     * @throws Exception
     */
    private Object getValue(Object o, String name) throws Exception {
        if (StringUtils.isNotNull(o) && StringUtils.isNotEmpty(name)) {
            Class<?> clazz = o.getClass();
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            o = field.get(o);
        }
        return o;
    }

    /**
     * 得到所有定义字段
     */
    private void createExcelField() {
        this.fields = new ArrayList<Object[]>();
        List<Field> tempFields = new ArrayList<>();
        tempFields.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
        tempFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        for (Field field : tempFields) {
            // 单注解
            if (field.isAnnotationPresent(Excel.class)) {
                putToField(field, field.getAnnotation(Excel.class));
            }

            // 多注解
            if (field.isAnnotationPresent(Excels.class)) {
                Excels attrs = field.getAnnotation(Excels.class);
                Excel[] excels = attrs.value();
                for (Excel excel : excels) {
                    putToField(field, excel);
                }
            }
        }
        this.fields = this.fields.stream().sorted(Comparator.comparing(objects -> ((Excel) objects[1]).sort())).collect(Collectors.toList());
        this.maxHeight = getRowHeight();
    }

    /**
     * 根据注解获取最大行高
     */
    public short getRowHeight() {
        double maxHeight = 0;
        for (Object[] os : this.fields) {
            Excel excel = (Excel) os[1];
            maxHeight = maxHeight > excel.height() ? maxHeight : excel.height();
        }
        return (short) (maxHeight * 20);
    }

    /**
     * 放到字段集合中
     */
    private void putToField(Field field, Excel attr) {
        if (attr != null && (attr.type() == Excel.Type.ALL || attr.type() == type)) {
            this.fields.add(new Object[]{field, attr});
        }
    }

    /**
     * 创建一个工作簿
     */
    public void createWorkbook() {
        this.wb = new SXSSFWorkbook(500);
    }

    /**
     * 创建工作表
     *
     * @param sheetNo sheet数量
     * @param index   序号
     */
    public void createSheet(double sheetNo, int index) {
        this.sheet = wb.createSheet();
        this.styles = createStyles(wb);
        // 设置工作表的名称.
        if (sheetNo == 0) {
            wb.setSheetName(index, sheetName);
        } else {
            wb.setSheetName(index, sheetName + index);
        }
    }

    /**
     * 获取单元格值
     *
     * @param row    获取的行
     * @param column 获取单元格列号
     * @return 单元格值
     */
    public Object getCellValue(Row row, int column) {
        if (row == null) {
            return row;
        }
        Object val = "";
        try {
            Cell cell = row.getCell(column);
            if (StringUtils.isNotNull(cell)) {
                if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
                    val = cell.getNumericCellValue();
                    if (DateUtil.isCellDateFormatted(cell)) {
                        val = DateUtil.getJavaDate((Double) val); // POI Excel 日期格式转换
                    } else {
                        if ((Double) val % 1 != 0) {
                            val = new BigDecimal(val.toString());
                        } else {
                            val = new DecimalFormat("0").format(val);
                        }
                    }
                } else if (cell.getCellType() == CellType.STRING) {
                    val = cell.getStringCellValue();
                } else if (cell.getCellType() == CellType.BOOLEAN) {
                    val = cell.getBooleanCellValue();
                } else if (cell.getCellType() == CellType.ERROR) {
                    val = cell.getErrorCellValue();
                }

            }
        } catch (Exception e) {
            return val;
        }
        return val;
    }

    /**
     * 根据java反射机制，取list中对象属性 创建内容单元格
     *
     * @param objectList list 集合
     * @param fields     fields 该list中 要导出的某些属性 String[] str = {"Cl0","name","code"};
     * @param fromRow    从第几行开始 -默认为0 从第一行开始,可选填
     *                   <p>
     *                   效果：通过list 显示整行整列的列表（无合并列、无合并行），可导出到Excel
     */
    @SuppressWarnings("unchecked")
    public void createTableByList(List objectList, String[] fields, int fromRow) throws Exception {
        int r = fromRow;//从fromRow行开始
        CellStyle style = wb.createCellStyle();//设置样式属性
        style.setBorderBottom(BorderStyle.THIN); //下边框
        style.setBorderLeft(BorderStyle.THIN);//左边框
        style.setBorderTop(BorderStyle.THIN);//上边框
        style.setBorderRight(BorderStyle.THIN);//右边框*/
        style.setAlignment(HorizontalAlignment.CENTER);//居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Row initRow = sheet.getRow(r);
        for (Object obj : objectList) {
            Class<?> classType = obj.getClass();
            Row row = null;
            if (r == fromRow && initRow != null) {
                row = initRow;// 设置行
            } else {
                row = sheet.createRow(r);// 设置行
            }

            for (int j = 0; j < fields.length; j++) {
                String firstLetter = fields[j].substring(0, 1).toUpperCase();//将属性首字母装换成大写
                String getMethodName = "get" + firstLetter + fields[j].substring(1);
                Method getMethod = classType.getMethod(getMethodName, new Class[]{});
                Object value = getMethod.invoke(obj, new Object[]{}); //获得对象的属性

                if (getMethod.isAnnotationPresent(JsonFormat.class)) {
                    System.out.println(fields[j]);
                }

                if (classType.getDeclaredField(fields[j]).isAnnotationPresent(JsonFormat.class)) {
                    JsonFormat jsonFormat = classType.getDeclaredField(fields[j]).getDeclaredAnnotation(JsonFormat.class);
                    if (StringUtils.isNotEmpty(jsonFormat.pattern()) && value != null) {
                        value = DateUtils.parseDateToStr(jsonFormat.pattern(), (Date) value);
                    }
                }


                Cell cell = null;
                if (initRow != null) {
                    if (r == fromRow) {
                        cell = row.getCell(j);
                        if (cell == null) {
                            cell = row.createCell(j);//创建单元格
                        }
                    } else {
                        cell = row.createCell(j);//创建单元格
                    }
                    //cell.setCellStyle(initRow.getCell(j).getCellStyle());
                    cell.setCellStyle(style);
                } else {
                    cell = row.createCell(j);//创建单元格
                    cell.setCellStyle(style);
                }

                cell.setCellValue(new XSSFRichTextString(value == null ? "" : value.toString()));
            }
            r = r + 1;
        }
    }

    /**
     * 输入EXCEL文件
     *
     * @param fileName 文件名
     * @author wdh
     */
    public String outputExcel(String fileName) {
        //String FileName =  fileName + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ConstantsUtils.EXTENSION_XLSX;//不重复文件名
        String FileName = fileName + ".xlsx";
        OutputStream out = null;
        try {
            out = new FileOutputStream(getAbsoluteFile(FileName));
            wb.write(out);

        } catch (Exception e) {
            log.error("导出Excel异常{}", e);
        } finally {

            try {
                if (wb != null) {
                    wb.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e1) {
                log.error("导出Excel异常{}", e1);
            }
        }
        return FileName;
    }

    /**
     * 获取Excel导出模板地址
     *
     * @return
     */
    public static InputStream getExcelPath(String fileName) throws IOException {
        Resource res = new ClassPathResource("excel/" + fileName + ".xlsx");
        return res.getInputStream();

    }

    public static void downFile(String fileName, HttpServletResponse response) {
        try {
            if (!FileUtils.checkAllowDownload(fileName)) {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = CommonConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通过文件名下载zip 文件
     *
     * @param fileName
     * @param response
     */
    public static void downZipFile(String fileName, HttpServletResponse response) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            String realFileName = fileName.substring(fileName.indexOf("_") + 1);
            String filePath = CommonConfig.getZipPath() + fileName;
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            byte[] buffer = new byte[4096];
            fis = new FileInputStream(filePath);
            bis = new BufferedInputStream(fis);
            OutputStream os = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                    FileUtils.deleteFile(CommonConfig.getZipPath() + fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /***
     * 删除指定文件夹下所有文件
     *
     * @param path 文件夹完整绝对路径
     * @return
     */
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @return 结果
     */
    public void exportExcelToOutputStream(HttpServletResponse response) {
        OutputStream out = null;
        try {
            out = response.getOutputStream();
            wb.write(out);
            return;
        } catch (Exception e) {
            log.error("导出Excel异常{}", e.getMessage());
            throw new CustomException("导出Excel失败，请联系网站管理员！");
        } finally {
            if (wb != null) {
                try {
                    wb.close();
                } catch (IOException e1) {
                    log.error("", e1);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    log.error("", e1);
                }
            }
        }
    }

    public CellStyle getNoticeStyle() {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 校验导入excel表头
     *
     * @param inSheet   导入文件sheet
     * @param headStart 表头开始行
     * @param headEnd   表头结束行
     * @param is        模板文件输入流
     * @param sheetNo   sheetNo
     */
    public void checkHead(Sheet inSheet, int headStart, int headEnd, InputStream is, Integer sheetNo) throws IOException {

        if (inSheet == null) {
            throw new IOException("文件sheet不存在");
        }
        // 获取模板表头规则
        Workbook isWorkbook = WorkbookFactory.create(is);
        Sheet isSheet;
        if (sheetNo != null) {
            // 如果指定sheetNo,则取指定sheet中的内容.
            isSheet = isWorkbook.getSheetAt(sheetNo);
        } else {
            // 如果传入的sheetNo不存在则默认指向第1个sheet.
            isSheet = isWorkbook.getSheetAt(0);
        }

        if (isSheet == null) {
            throw new IOException("模板文件sheet不存在");
        }
        // 模板表头Map
        Map<Integer, String> isHeadMap = getHeadMap(isSheet, headStart, headEnd);
        // 导入表头Map
        Map<Integer, String> inHeadMap = getHeadMap(inSheet, headStart, headEnd);
        checkHeadMap(inHeadMap, isHeadMap);
    }

    public void checkHead(Map<Integer, String> inHeadMap, int headStart, int headEnd, InputStream is, Integer sheetNo) {

        // 获取模板表头规则
        Workbook isWorkbook = null;
        try {
            isWorkbook = WorkbookFactory.create(is);
        } catch (IOException e) {
            log.error("excelImport error: ", e);
            throw new CustomException(e.getMessage());
        }
        Sheet isSheet;
        if (sheetNo != null) {
            // 如果指定sheetNo,则取指定sheet中的内容.
            isSheet = isWorkbook.getSheetAt(sheetNo);
        } else {
            // 如果传入的sheetNo不存在则默认指向第1个sheet.
            isSheet = isWorkbook.getSheetAt(0);
        }

        if (isSheet == null) {
            throw new CustomException("模板文件sheet不存在");
        }
        Map<Integer, String> isHeadMap = getHeadMap(isSheet, headStart, headEnd);
        checkHeadMap(inHeadMap, isHeadMap);
    }

    private void checkHeadMap(Map<Integer, String> inHeadMap, Map<Integer, String> isHeadMap) {

        Map<Integer, String> inMap = Maps.newHashMap(inHeadMap);
        Map<Integer, String> isMap = Maps.newHashMap(isHeadMap);

        if (inMap.size() > isMap.size()) {
            Iterator<Map.Entry<Integer, String>> iterator = inMap.entrySet().iterator();
            while (iterator.hasNext()) {
                checkHeadValue(inMap, isMap, iterator.next().getKey());
            }
        } else {
            Iterator<Map.Entry<Integer, String>> iterator = isMap.entrySet().iterator();
            while (iterator.hasNext()) {
                checkHeadValue(inMap, isMap, iterator.next().getKey());
            }
        }

        if (inMap.size() != isMap.size()) {
            throw new CustomException("模版不正确，请下载正确的模版重新上传！");
        }
    }

    private void checkHeadValue(Map<Integer, String> inHeadMap, Map<Integer, String> isHeadMap, Integer key) {

        if (inHeadMap.get(key) == null) {
            inHeadMap.remove(key);
        }
        if (isHeadMap.get(key) == null) {
            isHeadMap.remove(key);
        }
        if (!StringUtils.equals(inHeadMap.get(key), isHeadMap.get(key))) {
            throw new CustomException("模版不正确，请下载正确的模版重新上传！");
        }
    }

    public Map<Integer, String> getHeadMap(Sheet sheet, int headStart, int headEnd) {

        // 定义一个map用于存放excel列的序号和field.
        Map<Integer, String> headMap = new HashMap<>();
        // 获取表头
        List<Row> headList = new ArrayList<>();
        for (int i = headStart; i <= headEnd; i++) {
            Row headRow = sheet.getRow(i);
            if (Objects.nonNull(headRow)) {
                headList.add(headRow);
            }
        }
        if (CollUtil.isEmpty(headList)) {
            return headMap;
        }

        for (int i = 0; i < headList.get(0).getLastCellNum(); i++) {
            List<String> value = new ArrayList<>();
            for (Row row : headList) {
                String cellValue;
                boolean isMerge = isMergedRegion(sheet, row.getRowNum(), i);
                //判断是否具有合并单元格
                if (isMerge) {
                    cellValue = getMergedRegionValue(sheet, row.getRowNum(), i);
                } else {
                    //20210926 去除\n
                    cellValue = this.getCellValue(row, i).toString().replace("\n", "");
                }
                if (StringUtils.isNotEmpty(cellValue) && !value.contains(cellValue)) {
                    value.add(cellValue);
                }
            }
            if (CollUtil.isNotEmpty(value)) {
                headMap.put(i, StringUtils.join(value, "-"));
            }
        }
        return headMap;
    }

    /**
     * 通用校验上传Excel文件
     *
     * @param file 上传文件
     */
    public static void commonCheckUploadFile(MultipartFile file) {

        if (file == null) {
            throw new CustomException("上传文件为空");
        }
        // ①上传数据文件格式需与所选报表模板保持一致；
        String originalFilename = file.getOriginalFilename();
        boolean fileNameIsNull = Objects.isNull(originalFilename);
        boolean fileNotExcelFile = !StringUtils.endsWithIgnoreCase(originalFilename, XLS) &&
                !StringUtils.endsWithIgnoreCase(originalFilename, XLSX);
        if (fileNameIsNull || fileNotExcelFile) {
            throw new CustomException("模版不正确，请下载正确的模版重新上传！");
        }
    }

    public static void main(String[] args) throws Exception {


    }


}
