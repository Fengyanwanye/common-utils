package com.axin.common.utils.poi;

import com.axin.common.utils.poi.reader.BatchExcelReader;
import com.axin.framework.aspectj.lang.annotation.Excel;
import org.junit.Test;

import java.io.FileOutputStream;

/**
 * Excel门面类测试
 * 
 * @author fuchuanxin
 */
public class ExcelFacadeTest {
    
//    /**
//     * 测试基本导出
//     */
//    @Test
//    public void testBasicExport() throws Exception {
//        ExcelUsageExample example = new ExcelUsageExample();
//        example.example1_basicExport();
//    }
//
//    /**
//     * 测试大数据量分批导入
//     */
//    @Test
//    public void testBatchImport() throws Exception {
//        BatchImportExample example = new BatchImportExample();
//        // 注：这个测试需要实际的Excel文件，可以根据需要启用
//        // example.example1_basicBatchImport();
//        System.out.println("大数据量导入API已就绪，请参考 BatchImportExample 类中的示例");
//    }
//
//    /**
//     * 测试回调机制
//     */
//    @Test
//    public void testBatchCallback() {
//        // 测试回调接口的使用
//        BatchExcelReader.BatchCallback<Object> callback = (batch, batchNum, totalRead) -> {
//            System.out.println("批次: " + batchNum + ", 数据量: " + batch.size() + ", 总计: " + totalRead);
//            return true;
//        };
//
//        System.out.println("回调接口测试通过");
//    }

//    /**
//     * 测试导出模板（验证文本格式cellType生效）
//     */
//    @Test
//    public void testExportTemplate() throws Exception {
//        try (FileOutputStream fos = new FileOutputStream("test_template.xlsx")) {
//            ExcelFacade.exportTemplate("测试Sheet", TestEntity.class, fos);
//            System.out.println("模板导出成功：test_template.xlsx");
//            System.out.println("请打开文件验证：");
//            System.out.println("1. '计划月份'列：输入yyyy-MM格式（如2025-01），应保持为文本格式");
//            System.out.println("2. '数量'列：应显示为数值格式（0.00）");
//            System.out.println("3. '价格'列：应显示为数值格式（0.00）");
//        }
//    }
//
//    /**
//     * 测试实体类
//     */
//    public static class TestEntity {
//        @Excel(name = "姓名", sort = 1)
//        private String name;
//
//        @Excel(name = "计划月份", cellType = Excel.ColumnType.STRING, sort = 2)
//        private String planMonth;
//
//        @Excel(name = "数量", cellType = Excel.ColumnType.NUMERIC, sort = 3)
//        private Integer quantity;
//
//        @Excel(name = "价格", cellType = Excel.ColumnType.NUMERIC, sort = 4, align = Excel.Align.RIGHT)
//        private Double price;
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public String getPlanMonth() {
//            return planMonth;
//        }
//
//        public void setPlanMonth(String planMonth) {
//            this.planMonth = planMonth;
//        }
//
//        public Integer getQuantity() {
//            return quantity;
//        }
//
//        public void setQuantity(Integer quantity) {
//            this.quantity = quantity;
//        }
//
//        public Double getPrice() {
//            return price;
//        }
//
//        public void setPrice(Double price) {
//            this.price = price;
//        }
//    }
}
