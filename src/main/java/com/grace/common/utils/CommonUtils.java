package com.grace.common.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.grace.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;

import java.io.File;

/**
 * 通用工具类
 *
 * @author chanfa
 */
@Slf4j
public class CommonUtils {

    private static final Searcher SEARCHER;

    // 加载ip2region.xdb文件内容到内存
    static {
        String fileName = "/ip2region.xdb";
        File existFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
        if (!FileUtil.exist(existFile)) {
            ClassPathResource fileStream = new ClassPathResource(fileName);
            if (ObjectUtil.isEmpty(fileStream.getStream())) {
                throw new CustomException("ip2region.xdb文件不存在！");
            }
            FileUtil.writeFromStream(fileStream.getStream(), existFile);
        }
        try {
            byte[] cBuff = Searcher.loadContentFromFile(existFile.getPath());
            SEARCHER = Searcher.newWithBuffer(cBuff);
        } catch (Exception e) {
            log.error("从ip2region.xdb文件加载内容失败！原因：{}", e.getMessage());
            throw new CustomException("从ip2region.xdb文件加载内容失败！");
        }
    }

    /**
     * 根据IP查询地理位置
     *
     * @param ip ip地址
     * @return 地理位置
     */
    public static String getAddressByIp(String ip) {
        String unknown = "未知";
        // 判断是否IPV4
        if (Validator.isIpv4(ip)) {
            // 判断内外网
            if (NetUtil.isInnerIP(ip)) {
                return "内网IP";
            }
        } else {
            // 判断是否IPV6
            if (!Validator.isIpv6(ip)) {
                // 未知类型
                return unknown;
            }
        }
        try {
            // ip离线查询
            String region = SEARCHER.search(ip.trim());
            // 返回格式处理
            return region.replace("0|", StrUtil.EMPTY).replace("|0", StrUtil.EMPTY);
        } catch (Exception e) {
            log.error("IP地址离线获取城市异常 {}", ip);
            return "未知";
        }
    }
}
