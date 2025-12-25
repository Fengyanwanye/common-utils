package com.axin.common.utils.poi;

import com.axin.common.utils.poi.reader.BatchExcelReader;
import com.axin.framework.aspectj.lang.annotation.Excel;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 分批导入Excel使用示例
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/25
 */
public class BatchImportExample {

    /**
     * 示例实体类
     */
    public static class User {
        @Excel(name = "用户ID", type = Excel.Type.IMPORT, sort = 1)
        private Long id;

        @Excel(name = "用户名", type = Excel.Type.IMPORT, sort = 2)
        private String username;

        @Excel(name = "邮箱", type = Excel.Type.IMPORT, sort = 3)
        private String email;

        @Excel(name = "年龄", type = Excel.Type.IMPORT, sort = 4)
        private Integer age;

        @Excel(name = "创建时间", type = Excel.Type.IMPORT, dateFormat = "yyyy-MM-dd HH:mm:ss", sort = 5)
        private Date createTime;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public Date getCreateTime() { return createTime; }
        public void setCreateTime(Date createTime) { this.createTime = createTime; }
    }

    /**
     * 示例1：基本的分批导入
     */
    public void example1_basicBatchImport() throws Exception {
        // 假设有一个包含10万条数据的Excel文件
        InputStream inputStream = new FileInputStream("users.xlsx");
        
        // 分批读取，每批1000条
        int totalRows = ExcelFacade.importExcelBatch(
            inputStream,
            User.class,
            1000,  // 每批1000条
            (batch, batchNum, totalRead) -> {
                // 处理当前批次数据
                System.out.println("正在处理第 " + batchNum + " 批，本批数据量：" + batch.size());
                
                // 保存到数据库（这里仅示例）
                saveBatchToDatabase(batch);
                
                System.out.println("已处理 " + totalRead + " 条数据");
                
                // 返回true继续读取，false停止
                return true;
            }
        );
        
        System.out.println("导入完成，总共导入 " + totalRows + " 条数据");
    }

    /**
     * 示例2：带验证的分批导入
     */
    public void example2_batchImportWithValidation() throws Exception {
        InputStream inputStream = new FileInputStream("users.xlsx");
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> errors = new ArrayList<>();
        
        int totalRows = ExcelFacade.importExcelBatch(
            inputStream,
            User.class,
            500,
            (batch, batchNum, totalRead) -> {
                // 验证数据
                List<User> validUsers = new ArrayList<>();
                
                for (User user : batch) {
                    if (validateUser(user)) {
                        validUsers.add(user);
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                        errors.add("第" + totalRead + "行：用户数据验证失败 - " + user.getUsername());
                    }
                }
                
                // 保存验证通过的数据
                if (!validUsers.isEmpty()) {
                    saveBatchToDatabase(validUsers);
                }
                
                System.out.println("批次 " + batchNum + " 处理完成：成功 " + validUsers.size() + "，失败 " + (batch.size() - validUsers.size()));
                
                return true;
            }
        );
        
        System.out.println("===== 导入结果汇总 =====");
        System.out.println("总行数：" + totalRows);
        System.out.println("成功：" + successCount.get());
        System.out.println("失败：" + failCount.get());
        if (!errors.isEmpty()) {
            System.out.println("错误详情：");
            errors.forEach(System.out::println);
        }
    }

    /**
     * 示例3：在Controller中使用（Spring MVC/Boot）
     */
    // @RestController
    // @RequestMapping("/api/users")
    // public class UserController {
    //
    //     @Autowired
    //     private UserService userService;
    //
    //     /**
    //      * 分批导入用户数据（大数据量）
    //      */
    //     @PostMapping("/import/batch")
    //     public Result batchImport(@RequestParam("file") MultipartFile file) {
    //         try {
    //             InputStream inputStream = file.getInputStream();
    //             
    //             AtomicInteger successCount = new AtomicInteger(0);
    //             AtomicInteger failCount = new AtomicInteger(0);
    //             
    //             int totalRows = ExcelFacade.importExcelBatch(
    //                 inputStream,
    //                 User.class,
    //                 1000,  // 每批1000条
    //                 (batch, batchNum, totalRead) -> {
    //                     try {
    //                         // 使用Service批量保存
    //                         userService.saveBatch(batch);
    //                         successCount.addAndGet(batch.size());
    //                         
    //                         // 记录日志
    //                         log.info("成功导入第{}批，共{}条数据，累计{}条", batchNum, batch.size(), totalRead);
    //                         
    //                         return true;
    //                     } catch (Exception e) {
    //                         log.error("第{}批数据导入失败", batchNum, e);
    //                         failCount.addAndGet(batch.size());
    //                         return true;  // 继续处理下一批
    //                     }
    //                 }
    //             );
    //             
    //             return Result.success("导入完成：总计" + totalRows + "条，成功" + successCount.get() + "条，失败" + failCount.get() + "条");
    //             
    //         } catch (Exception e) {
    //             log.error("导入失败", e);
    //             return Result.error("导入失败：" + e.getMessage());
    //         }
    //     }
    // }

    /**
     * 示例4：使用try-with-resources管理资源
     */
    public void example4_resourceManagement() throws Exception {
        InputStream inputStream = new FileInputStream("users.xlsx");
        
        // 使用try-with-resources自动关闭资源
        try (BatchExcelReader<User> reader = ExcelFacade.createBatchReader(User.class)) {
            
            reader.readBatch(inputStream, 1000, (batch, batchNum, totalRead) -> {
                saveBatchToDatabase(batch);
                return true;
            });
            
            System.out.println("总共读取：" + reader.getReadRows() + " 行");
        }
        // reader会自动关闭，释放资源
    }

    /**
     * 示例5：指定Sheet和自定义表头位置
     */
    public void example5_customSheetAndHeader() throws Exception {
        InputStream inputStream = new FileInputStream("users.xlsx");
        
        // Excel文件有2行表头（第0行和第1行），数据从第2行开始
        int totalRows = ExcelFacade.importExcelBatch(
            inputStream,
            "用户数据",  // Sheet名称
            0,          // 表头起始行
            1,          // 表头结束行
            2,          // 数据起始行
            User.class,
            500,        // 批次大小
            (batch, batchNum, totalRead) -> {
                saveBatchToDatabase(batch);
                return true;
            }
        );
        
        System.out.println("导入完成：" + totalRows + " 条");
    }

    /**
     * 示例6：带进度监控的分批导入
     */
    public void example6_withProgress() throws Exception {
        InputStream inputStream = new FileInputStream("users.xlsx");
        
        // 假设预先知道大约有10万条数据
        int estimatedTotal = 100000;
        
        int totalRows = ExcelFacade.importExcelBatch(
            inputStream,
            User.class,
            1000,
            (batch, batchNum, totalRead) -> {
                // 保存数据
                saveBatchToDatabase(batch);
                
                // 计算进度
                int progress = (int) (totalRead * 100.0 / estimatedTotal);
                System.out.println("导入进度：" + progress + "%，已导入：" + totalRead + " 条");
                
                return true;
            }
        );
        
        System.out.println("导入完成！实际导入：" + totalRows + " 条");
    }

    /**
     * 示例7：条件停止导入
     */
    public void example7_conditionalStop() throws Exception {
        InputStream inputStream = new FileInputStream("users.xlsx");
        
        AtomicInteger errorCount = new AtomicInteger(0);
        int maxErrors = 100;  // 最大允许100个错误
        
        int totalRows = ExcelFacade.importExcelBatch(
            inputStream,
            User.class,
            500,
            (batch, batchNum, totalRead) -> {
                for (User user : batch) {
                    try {
                        if (!validateUser(user)) {
                            errorCount.incrementAndGet();
                        } else {
                            saveToDatabase(user);
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    }
                    
                    // 如果错误太多，停止导入
                    if (errorCount.get() >= maxErrors) {
                        System.err.println("错误数量超过限制(" + maxErrors + ")，停止导入");
                        return false;  // 返回false停止读取
                    }
                }
                
                System.out.println("已处理 " + totalRead + " 条，错误 " + errorCount.get() + " 条");
                return true;
            }
        );
        
        System.out.println("导入结束：总计 " + totalRows + " 条，错误 " + errorCount.get() + " 条");
    }

    /**
     * 示例8：多线程并行处理（高级用法）
     */
    public void example8_parallelProcessing() throws Exception {
        InputStream inputStream = new FileInputStream("users.xlsx");
        
        // 注意：这里只是演示概念，实际使用需要考虑线程安全
        int totalRows = ExcelFacade.importExcelBatch(
            inputStream,
            User.class,
            1000,
            (batch, batchNum, totalRead) -> {
                // 可以在这里使用线程池异步处理
                // executorService.submit(() -> {
                //     saveBatchToDatabase(batch);
                // });
                
                saveBatchToDatabase(batch);
                return true;
            }
        );
        
        System.out.println("导入完成：" + totalRows + " 条");
    }

    // ==================== 辅助方法 ====================

    /**
     * 验证用户数据
     */
    private boolean validateUser(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return false;
        }
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            return false;
        }
        if (user.getAge() == null || user.getAge() < 0 || user.getAge() > 150) {
            return false;
        }
        return true;
    }

    /**
     * 批量保存到数据库
     */
    private void saveBatchToDatabase(List<User> users) {
        // 模拟保存到数据库
        // 实际项目中应该调用Service或DAO层
        // userService.saveBatch(users);
        
        System.out.println("保存 " + users.size() + " 条数据到数据库");
    }

    /**
     * 保存单条数据到数据库
     */
    private void saveToDatabase(User user) {
        // userService.save(user);
    }

    /**
     * 核心优势说明：
     * 
     * 1. 内存友好：边读边处理，不需要一次性加载所有数据到内存
     * 2. 灵活的批处理：支持自定义批次大小
     * 3. 实时回调：每批数据处理完立即回调，可实时反馈进度
     * 4. 条件中断：支持在回调中返回false停止读取
     * 5. 资源管理：实现Closeable接口，支持try-with-resources
     * 6. 错误处理：可以在回调中捕获异常，决定是否继续
     * 7. 数据验证：可以在回调中验证数据，过滤无效数据
     * 8. 灵活集成：可与Spring、MyBatis-Plus等框架无缝集成
     * 
     * 使用场景：
     * - 导入大批量数据（10万+）
     * - 需要实时显示导入进度
     * - 需要对数据进行验证和清洗
     * - 需要根据条件中断导入
     * - 需要批量插入数据库提高性能
     */
}
