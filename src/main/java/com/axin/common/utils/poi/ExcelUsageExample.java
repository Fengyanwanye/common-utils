package com.axin.common.utils.poi;

import com.axin.framework.aspectj.lang.annotation.Excel;

import java.util.Date;

/**
 * Excel使用示例 - 演示如何使用重构后的Excel工具
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/24
 */
public class ExcelUsageExample {

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

        @Excel(name = "年龄", sort = 4, align = Excel.Align.CENTER)
        private Integer age;

        @Excel(name = "状态", sort = 5, readConverterExp = "0=禁用,1=启用")
        private Integer status;

        @Excel(name = "创建时间", sort = 6, dateFormat = "yyyy-MM-dd HH:mm:ss", width = 25)
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
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public Date getCreateTime() { return createTime; }
        public void setCreateTime(Date createTime) { this.createTime = createTime; }
    }

    /**
     * 示例1：基本的Excel导出
     */
    public void example1_basicExport() throws Exception {
        // 准备数据
        java.util.List<User> users = new java.util.ArrayList<>();
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("张三");
        user1.setEmail("zhangsan@example.com");
        user1.setAge(25);
        user1.setStatus(1);
        user1.setCreateTime(new Date());
        users.add(user1);

        // 导出Excel到文件
        String filename = ExcelFacade.exportExcel(users, "用户列表", User.class);
        System.out.println("导出成功，文件名：" + filename);
    }

    /**
     * 示例2：导出Excel到HTTP响应
     */
    public void example2_exportToResponse() throws Exception {
        // 在Controller中使用
        // @GetMapping("/export")
        // public void export(HttpServletResponse response) throws Exception {
        //     List<User> users = userService.list();
        //     
        //     // 设置响应头
        //     response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        //     response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");
        //     
        //     // 导出
        //     ExcelFacade.exportExcel(users, "用户列表", User.class, response);
        // }
    }

    /**
     * 示例3：基本的Excel导入
     */
    public void example3_basicImport() throws Exception {
        // 在Controller中使用
        // @PostMapping("/import")
        // public Result importUsers(@RequestParam("file") MultipartFile file) throws Exception {
        //     InputStream inputStream = file.getInputStream();
        //     List<User> users = ExcelFacade.importExcel(inputStream, User.class);
        //     
        //     // 保存数据
        //     userService.saveBatch(users);
        //     return Result.success("导入成功，共" + users.size() + "条数据");
        // }
    }

    /**
     * 示例4：导入Excel（自定义表头位置）
     */
    public void example4_importWithCustomHeader() throws Exception {
        // 假设Excel文件有2行表头（第0行和第1行），数据从第2行开始
        // InputStream inputStream = ...;
        // List<User> users = ExcelFacade.importExcel(
        //     inputStream, 
        //     "用户数据",  // Sheet名称
        //     0,          // 表头起始行
        //     1,          // 表头结束行
        //     2,          // 数据起始行
        //     User.class
        // );
    }

    /**
     * 示例5：生成导入模板
     */
    public void example5_exportTemplate() throws Exception {
        // 生成模板文件
        String filename = ExcelFacade.exportTemplate("用户导入模板", User.class);
        System.out.println("模板生成成功：" + filename);

        // 或者直接输出到HTTP响应
        // @GetMapping("/template")
        // public void downloadTemplate(HttpServletResponse response) throws Exception {
        //     response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        //     response.setHeader("Content-Disposition", "attachment; filename=user-template.xlsx");
        //     ExcelFacade.exportTemplate("用户导入模板", User.class, response);
        // }
    }

    /**
     * 示例6：使用自定义Reader/Writer（高级用法）
     */
    public void example6_customReaderWriter() throws Exception {
        // 创建自定义的Reader
        // ExcelReader<User> reader = ExcelFacade.createReader(User.class);
        // InputStream inputStream = ...;
        // List<User> users = reader.read(inputStream, "Sheet1");
        
        // 创建自定义的Writer
        // ExcelWriter<User> writer = ExcelFacade.createWriter(User.class);
        // List<User> users = ...;
        // String filename = writer.write(users, "用户列表");
    }

    /**
     * 完整的Controller示例
     */
    // @RestController
    // @RequestMapping("/api/users")
    // public class UserController {
    //     
    //     @Autowired
    //     private UserService userService;
    //     
    //     /**
    //      * 导出用户列表
    //      */
    //     @GetMapping("/export")
    //     public void export(HttpServletResponse response) throws Exception {
    //         List<User> users = userService.list();
    //         
    //         response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    //         response.setCharacterEncoding("utf-8");
    //         String fileName = URLEncoder.encode("用户列表", "UTF-8");
    //         response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
    //         
    //         ExcelFacade.exportExcel(users, "用户列表", User.class, response);
    //     }
    //     
    //     /**
    //      * 导入用户数据
    //      */
    //     @PostMapping("/import")
    //     public Result importUsers(@RequestParam("file") MultipartFile file) throws Exception {
    //         if (file.isEmpty()) {
    //             return Result.error("上传文件为空");
    //         }
    //         
    //         InputStream inputStream = file.getInputStream();
    //         List<User> users = ExcelFacade.importExcel(inputStream, User.class);
    //         
    //         if (users.isEmpty()) {
    //             return Result.error("文件中没有数据");
    //         }
    //         
    //         userService.saveBatch(users);
    //         return Result.success("导入成功，共" + users.size() + "条数据");
    //     }
    //     
    //     /**
    //      * 下载导入模板
    //      */
    //     @GetMapping("/template")
    //     public void downloadTemplate(HttpServletResponse response) throws Exception {
    //         response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    //         response.setCharacterEncoding("utf-8");
    //         String fileName = URLEncoder.encode("用户导入模板", "UTF-8");
    //         response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
    //         
    //         ExcelFacade.exportTemplate("用户导入模板", User.class, response);
    //     }
    // }
}
