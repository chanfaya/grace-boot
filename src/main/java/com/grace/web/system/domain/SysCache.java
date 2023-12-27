package com.grace.web.system.domain;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * 缓存信息
 *
 * @author chanfa
 */
@Data
public class SysCache {

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 缓存键名
     */
    private String cacheKey = StrUtil.EMPTY;

    /**
     * 缓存内容
     */
    private String cacheValue = StrUtil.EMPTY;

    /**
     * 备注
     */
    private String remark = StrUtil.EMPTY;

    public SysCache(String cacheName, String remark) {
        this.cacheName = cacheName;
        this.remark = remark;
    }

    public SysCache(String cacheName, String cacheKey, String cacheValue) {
        this.cacheName = StrUtil.replace(cacheName, StrUtil.COLON, StrUtil.EMPTY);
        this.cacheKey = StrUtil.replace(cacheKey, cacheName, StrUtil.EMPTY);
        this.cacheValue = cacheValue;
    }
}
