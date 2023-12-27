package com.grace.common.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.grace.common.constant.CacheConstants;
import com.grace.common.utils.redis.RedisUtils;
import com.grace.web.system.domain.SysDictData;

import java.util.Collection;
import java.util.List;

/**
 * 字典工具类
 *
 * @author chanfa
 */
public class DictUtils {

    /**
     * 设置字典缓存
     *
     * @param key  参数键
     * @param list 字典数据列表
     */
    public static void setDictCache(String key, List<SysDictData> list) {
        RedisUtils.setCacheObject(getCacheKey(key), list);
    }

    /**
     * 获取字典缓存
     *
     * @param key 参数键
     * @return list 字典数据列表
     */
    public static List<SysDictData> getDictCache(String key) {
        Object cacheObj = RedisUtils.getCacheObject(getCacheKey(key));
        if (ObjectUtil.isNotNull(cacheObj)) {
            return Convert.toList(SysDictData.class, cacheObj);
        }
        return null;
    }

    /**
     * 删除指定字典缓存
     *
     * @param key 字典键
     */
    public static void removeDictCache(String key) {
        RedisUtils.deleteObject(getCacheKey(key));
    }

    /**
     * 清空字典缓存
     */
    public static void clearDictCache() {
        Collection<String> keys = RedisUtils.keys(CacheConstants.SYS_DICT_KEY + "*");
        RedisUtils.deleteObject(keys);
    }

    /**
     * 设置cache key
     *
     * @param configKey 参数键
     * @return 缓存键key
     */
    public static String getCacheKey(String configKey) {
        return CacheConstants.SYS_DICT_KEY + configKey;
    }
}
