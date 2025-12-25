package com.axin.common.utils.poi.helper;

import com.axin.common.utils.file.FileUtils;
import com.axin.framework.config.CommonConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Excel文件操作助手
 *
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/12/24
 */
public class ExcelFileHelper {

    private static final Logger log = LoggerFactory.getLogger(ExcelFileHelper.class);

    /**
     * 下载Excel文件
     *
     * @param fileName 文件名
     * @param response HTTP响应
     */
    public static void downloadFile(String fileName, HttpServletResponse response) {
        try {
            if (!FileUtils.checkAllowDownload(fileName)) {
                throw new Exception("文件名称非法，不允许下载");
            }

            String realFileName = System.currentTimeMillis() + 
                fileName.substring(fileName.indexOf("_") + 1);
            String filePath = CommonConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 下载ZIP文件
     *
     * @param fileName 文件名
     * @param response HTTP响应
     */
    public static void downloadZipFile(String fileName, HttpServletResponse response) {
        try (FileInputStream fis = new FileInputStream(CommonConfig.getZipPath() + fileName);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            String realFileName = fileName.substring(fileName.indexOf("_") + 1);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);

            byte[] buffer = new byte[4096];
            OutputStream os = response.getOutputStream();
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            log.error("下载ZIP文件失败", e);
        } finally {
            // 下载完成后删除临时文件
            try {
                FileUtils.deleteFile(CommonConfig.getZipPath() + fileName);
            } catch (Exception e) {
                log.error("删除临时文件失败", e);
            }
        }
    }

    /**
     * 删除指定目录下所有文件
     *
     * @param path 目录路径
     * @return 是否成功
     */
    public static boolean deleteAllFiles(String path) {
        File directory = new File(path);
        
        if (!directory.exists() || !directory.isDirectory()) {
            return false;
        }

        String[] fileList = directory.list();
        if (fileList == null) {
            return false;
        }

        for (String fileName : fileList) {
            File file = new File(path + File.separator + fileName);
            
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                deleteAllFiles(file.getAbsolutePath());
            }
        }

        return true;
    }

    /**
     * 获取Excel模板文件输入流
     *
     * @param templateName 模板文件名（不含扩展名）
     * @return 输入流
     * @throws IOException IO异常
     */
    public static InputStream getTemplateStream(String templateName) throws IOException {
        Resource resource = new ClassPathResource("excel/" + templateName + ".xlsx");
        return resource.getInputStream();
    }

    /**
     * 确保下载目录存在
     *
     * @param filePath 文件路径
     */
    public static void ensureDirectoryExists(String filePath) {
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 扩展名（含点号）
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }

        return fileName.substring(lastDotIndex);
    }

    /**
     * 验证是否为Excel文件
     *
     * @param fileName 文件名
     * @return 是否为Excel文件
     */
    public static boolean isExcelFile(String fileName) {
        if (fileName == null) {
            return false;
        }

        String extension = getFileExtension(fileName).toLowerCase();
        return ".xls".equals(extension) || ".xlsx".equals(extension);
    }
}
