package com.axin.common.utils.file;

import com.axin.common.utils.StringUtils;
import com.axin.framework.config.CommonConfig;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

/**
 * @author fuchuanxin
 * @version 1.0
 * @description: TODO
 * @date 2025/12/23 16:56
 */
public class ImageUtils {

    private static final Logger log = LoggerFactory.getLogger(ImageUtils.class);

    public static byte[] getImage(String imagePath) {
        InputStream is = getFile(imagePath);

        try {
            return IOUtils.toByteArray(is);
        } catch (Exception e) {
            log.error("图片加载异常 {}", e);
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static InputStream getFile(String imagePath) {
        try {
            byte[] result = readFile(imagePath);
            result = Arrays.copyOf(result, result.length);
            return new ByteArrayInputStream(result);
        } catch (Exception e) {
            log.error("获取图片异常 {}", e);
            return null;
        }
    }

    public static byte[] readFile(String url) {
        InputStream in = null;
        ByteArrayOutputStream baos = null;

        try {
            if (url.startsWith("http")) {
                URL urlObj = new URL(url);
                URLConnection urlConnection = urlObj.openConnection();
                urlConnection.setConnectTimeout(30000);
                urlConnection.setReadTimeout(60000);
                urlConnection.setDoInput(true);
                in = urlConnection.getInputStream();
            } else {
                String localPath = CommonConfig.getProfile();
                String downloadPath = localPath + StringUtils.substringAfter(url, "/profile");
                in = new FileInputStream(downloadPath);
            }
            return IOUtils.toByteArray(in);
        } catch (Exception e) {
            log.error("获取文件路径异常 {}", e);
            return null;
        } finally {
            IOUtils.closeQuietly(baos);
        }
    }
}
