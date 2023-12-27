package com.grace.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 *
 * @author chanfa
 */
@Component
public class CommonConfig {

    /**
     * 头像路径
     */
    public static final String AVATAR = "/avatar/";

    /**
     * 下载路径
     */
    public static final String DOWNLOAD = "/download/";

    /**
     * 上传路径
     */
    public static final String UPLOAD = "/upload/";

    /**
     * 资源名称
     */
    @Getter
    private static String name;

    /**
     * 资源路径
     */
    @Getter
    private static String profile;

    @Value("${common.name}")
    public void setName(String name) {
        CommonConfig.name = name;
    }

    @Value("${common.profile}")
    public void setProfile(String profile) {
        CommonConfig.profile = profile;
    }

    /**
     * 获取头像上传路径
     */
    public static String getAvatarPath() {
        return getProfile() + AVATAR;
    }

    /**
     * 获取下载路径
     */
    public static String getDownloadPath() {
        return getProfile() + DOWNLOAD;
    }

    /**
     * 获取上传路径
     */
    public static String getUploadPath() {
        return getProfile() + UPLOAD;
    }
}
