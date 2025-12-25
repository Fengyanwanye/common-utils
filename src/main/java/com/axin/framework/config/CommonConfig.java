package com.axin.framework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author fuchuanxin
 * @version 1.0
 * @description: TODO
 * @date 2025/12/23 17:39
 */
@Component
@ConfigurationProperties(prefix = "com.common")
public class CommonConfig {

    /**
     * 项目名称
     */
    private String name;
    /**
     * 版本
     */
    private String version;
    /**
     * 版权年份
     */
    private String copyrightYear;
    /**
     * 文件路径
     */
    private static String profile;
    /**
     * ip地址开关 true开启，false关闭
     */
    private static boolean addressEnabled;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCopyrightYear() {
        return this.copyrightYear;
    }

    public void setCopyrightYear(String copyrightYear) {
        this.copyrightYear = copyrightYear;
    }

    public static String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        CommonConfig.profile = profile;
    }

    public static boolean isAddressEnabled() {
        return addressEnabled;
    }

    public void setAddressEnabled(boolean addressEnabled) {
        CommonConfig.addressEnabled = addressEnabled;
    }

    /**
     * 获取下载路径
     */
    public static String getDownloadPath() {
        return getProfile() + "/download/";
    }

    /**
     * 压缩包所在路径
     */
    public static String getZipPath() {
        return getProfile() + "/zip/";
    }

    /**
     * 获取上传路径
     */
    public static String getUploadPath() {
        return getProfile() + "/upload";
    }

    /**
     * 获取报表所在路径
     */
    public static String getBbDirPath() {
        return getProfile() + "/templates/";
    }
}
