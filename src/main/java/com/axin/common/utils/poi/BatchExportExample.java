package com.axin.common.utils.poi;

import com.axin.common.utils.poi.writer.BatchExcelWriter;
import com.axin.framework.aspectj.lang.annotation.Excel;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 分批导出Excel使用示例
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/24
 */
public class BatchExportExample {

    /**
     * 示例实体类
     */
    public static class User {
        @Excel(name = "用户ID", sort = 1)
        private Long id;

        @Excel(name = "用户名", sort = 2, width = 20)
        private String username;

        @Excel(name = "邮箱", sort = 3, width = 30)
        private String email;

        @Excel(name = "年龄", sort = 4)
        private Integer age;

        @Excel(name = "创建时间", sort = 5, dateFormat = "yyyy-MM-dd HH:mm:ss", width = 25)
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
     * 示例1：基本的分批导出到文件
     */
    public void example1_batchExportToFile() throws Exception {
        // 假设有10万条数据，分批导出
        int totalCount = 100000;
        int batchSize = 1000;
        int totalPages = (totalCount + batchSize - 1) / batchSize;

        // 创建分批Writer
        try (BatchExcelWriter<User> writer = ExcelFacade.createBatchWriter(User.class, "用户列表")) {
            
            // 分批写入数据
            for (int page = 0; page < totalPages; page++) {
                // 模拟从数据库分页查询数据
                List<User> batch = queryUsersFromDatabase(page, batchSize);
                
                // 写入这批数据
                writer.writeBatch(batch);
                
                System.out.println("已写入第 " + (page + 1) + " 批，共 " + batch.size() + " 条数据");
            }
            
            // 完成并保存文件
            String filename = writer.finish();
            System.out.println("导出完成，文件名：" + filename);
            System.out.println("总共导出：" + writer.getWrittenRows() + " 行数据");
        }
    }

    /**
     * 示例2：分批导出到HTTP响应
     */
    public void example2_batchExportToResponse(HttpServletResponse response) throws Exception {
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=users.xlsx");

        int totalCount = 50000;
        int batchSize = 500;
        int totalPages = (totalCount + batchSize - 1) / batchSize;

        // 创建分批Writer
        try (BatchExcelWriter<User> writer = ExcelFacade.createBatchWriter(User.class, "用户数据")) {
            
            // 分批写入
            for (int page = 0; page < totalPages; page++) {
                List<User> batch = queryUsersFromDatabase(page, batchSize);
                writer.writeBatch(batch);
            }
            
            // 输出到响应流
            writer.finish(response.getOutputStream());
        }
    }

    /**
     * 示例3：在Controller中使用
     */
    // @RestController
    // @RequestMapping("/api/users")
    // public class UserController {
    //
    //     @Autowired
    //     private UserService userService;
    //
    //     /**
    //      * 分批导出用户列表（大数据量）
    //      */
    //     @GetMapping("/export/batch")
    //     public void batchExport(HttpServletResponse response) throws Exception {
    //         response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    //         response.setCharacterEncoding("utf-8");
    //         String fileName = URLEncoder.encode("用户列表", "UTF-8");
    //         response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
    //
    //         // 查询总数
    //         long totalCount = userService.count();
    //         int batchSize = 1000;
    //         int totalPages = (int) ((totalCount + batchSize - 1) / batchSize);
    //
    //         // 创建分批Writer
    //         try (BatchExcelWriter<User> writer = ExcelFacade.createBatchWriter(User.class, "用户列表")) {
    //             // 分批查询并写入
    //             for (int page = 0; page < totalPages; page++) {
    //                 // 使用MyBatis-Plus分页查询
    //                 Page<User> pageData = new Page<>(page + 1, batchSize);
    //                 List<User> users = userService.page(pageData).getRecords();
    //                 
    //                 writer.writeBatch(users);
    //             }
    //             
    //             // 输出到响应
    //             writer.finish(response.getOutputStream());
    //         }
    //     }
    // }

    /**
     * 示例4：带进度监控的分批导出
     */
    public void example4_batchExportWithProgress() throws Exception {
        int totalCount = 100000;
        int batchSize = 1000;
        int totalPages = (totalCount + batchSize - 1) / batchSize;

        try (BatchExcelWriter<User> writer = ExcelFacade.createBatchWriter(User.class, "用户列表")) {
            
            for (int page = 0; page < totalPages; page++) {
                List<User> batch = queryUsersFromDatabase(page, batchSize);
                writer.writeBatch(batch);
                
                // 计算并显示进度
                int progress = (int) ((page + 1) * 100.0 / totalPages);
                System.out.println("导出进度：" + progress + "%，已写入：" + writer.getWrittenRows() + " 行");
            }
            
            String filename = writer.finish();
            System.out.println("导出完成！文件：" + filename);
        }
    }

    /**
     * 示例5：错误处理
     */
    public void example5_errorHandling() {
        BatchExcelWriter<User> writer = null;
        
        try {
            writer = ExcelFacade.createBatchWriter(User.class, "用户列表");
            
            int page = 0;
            while (true) {
                List<User> batch = queryUsersFromDatabase(page, 1000);
                
                if (batch.isEmpty()) {
                    break;
                }
                
                writer.writeBatch(batch);
                page++;
            }
            
            String filename = writer.finish();
            System.out.println("导出成功：" + filename);
            
        } catch (Exception e) {
            System.err.println("导出失败：" + e.getMessage());
            e.printStackTrace();
        } finally {
            // 确保资源被释放
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 模拟从数据库查询数据
     */
    private List<User> queryUsersFromDatabase(int page, int size) {
        List<User> users = new ArrayList<>();
        
        int start = page * size;
        int end = Math.min(start + size, 100000);
        
        for (int i = start; i < end; i++) {
            User user = new User();
            user.setId((long) i);
            user.setUsername("user" + i);
            user.setEmail("user" + i + "@example.com");
            user.setAge(20 + (i % 50));
            user.setCreateTime(new Date());
            users.add(user);
        }
        
        return users;
    }

    /**
     * 核心优势说明：
     * 
     * 1. 内存友好：不需要一次性加载所有数据到内存
     * 2. 性能优化：使用SXSSFWorkbook流式写入
     * 3. 自动分Sheet：超过65536行自动创建新Sheet
     * 4. 支持进度监控：可以实时获取已写入的行数
     * 5. 资源管理：实现Closeable接口，支持try-with-resources
     */
}
