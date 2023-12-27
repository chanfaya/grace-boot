package com.grace.web.controller.common;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.grace.common.config.CommonConfig;
import com.grace.common.constant.Constants;
import com.grace.common.core.domain.AjaxResult;
import com.grace.framework.config.ServerConfig;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通用请求处理
 *
 * @author chanfa
 */
@Slf4j
@RestController
public class CommonController {

    @Resource
    private ServerConfig serverConfig;

    /**
     * 通用下载请求
     *
     * @param fileName 文件名称
     * @param delete   是否删除
     */
    @GetMapping("common/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response) {
        try {
            String filePath = CommonConfig.getDownloadPath() + fileName;
            // 设置响应头
            setAttachmentResponseHeader(response, fileName);
            // 下载
            FileUtil.writeToStream(filePath, response.getOutputStream());
            if (delete) {
                FileUtil.del(filePath);
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求
     *
     * @param file 上传文件
     * @return 结果
     */
    @PostMapping("/common/upload")
    public AjaxResult uploadFile(MultipartFile file) {
        try {
            // 上传文件路径
            String filePath = CommonConfig.getUploadPath();
            // 重命名
            String fileName = exist(filePath, file.getOriginalFilename());
            // 上传
            FileUtil.writeFromStream(file.getInputStream(), filePath + fileName);
            // 返回文件访问路径
            String url = serverConfig.getUrl() + Constants.RESOURCE_PREFIX + CommonConfig.UPLOAD + fileName;
            AjaxResult ajax = AjaxResult.success("上传成功");
            ajax.put("fileName", fileName);
            ajax.put("url", url);
            return ajax;
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 本地资源通用下载
     *
     * @param resource 资源
     * @param response 结果
     */
    @GetMapping("/common/download/resource")
    public void resourceDownload(String resource, HttpServletResponse response) {
        try {
            // 本地资源路径
            String localPath = CommonConfig.getProfile();
            // 数据库资源地址
            String downloadPath = localPath + StrUtil.subAfter(resource, Constants.RESOURCE_PREFIX, false);
            // 下载名称
            String downloadName = StrUtil.subAfter(downloadPath, StrUtil.SLASH, true);
            // 设置响应头
            setAttachmentResponseHeader(response, downloadName);
            // 下载
            FileUtil.writeToStream(downloadPath, response.getOutputStream());
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 设置响应头
     *
     * @param response     响应
     * @param realFileName 文件名
     */
    private void setAttachmentResponseHeader(HttpServletResponse response, String realFileName) {
        String encode = URLUtil.encode(realFileName);
        String percentEncodedFileName = encode.replaceAll("\\+", "%20");
        String contentDispositionValue = "attachment; filename=" + percentEncodedFileName +
                ";filename*=utf-8''" + percentEncodedFileName;
        response.setHeader("Content-Disposition", contentDispositionValue);
    }

    /**
     * 判断指定路径下的文件是否存在：存在则自动重复名
     *
     * @param filePath 文件路径
     * @param fileName 文件名
     * @return 新文件名
     */
    private String exist(String filePath, String fileName) {
        if (FileUtil.exist(filePath + fileName)) {
            // 文件名中最后一个点(.)的位置
            int dot = fileName.lastIndexOf(StrUtil.DOT);
            // 文件名（不含后缀）
            String name = fileName.substring(0, dot);
            // 文件后缀（.txt）
            String suffix = fileName.substring(dot);
            // 重命名
            fileName = name + "(1)" + suffix;
            // 递归判断
            return exist(filePath, fileName);
        }
        return fileName;
    }
}
