package com.grace.common.utils.redis;

import cn.hutool.core.convert.Convert;
import cn.hutool.extra.spring.SpringUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.redisson.api.RMap;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Set;

/**
 * 缓存操作工具类 {@link }
 *
 * @author chanfa
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheUtils {

    private static final CacheManager CACHE_MANAGER = SpringUtil.getBean(CacheManager.class);

    /**
     * 获取缓存组内所有的KEY
     *
     * @param cacheNames 缓存组名称
     */
    public static Set<Object> keys(String cacheNames) {
        Cache cache = CACHE_MANAGER.getCache(cacheNames);
        if (cache == null) {
            return null;
        }
        RMap<Object, Object> rmap = Convert.convertByClassName(RMap.class.getName(), cache.getNativeCache());
        return rmap.keySet();
    }

    /**
     * 获取缓存值
     *
     * @param cacheNames 缓存组名称
     * @param key        缓存key
     */
    public static Object get(String cacheNames, Object key) {
        Cache cache = CACHE_MANAGER.getCache(cacheNames);
        if (cache == null) {
            return null;
        }
        Cache.ValueWrapper wrapper = cache.get(key);
        return wrapper == null ? null : wrapper.get();
    }

    /**
     * 删除缓存值
     *
     * @param cacheNames 缓存组名称
     * @param key        缓存key
     */
    public static void evict(String cacheNames, Object key) {
        Cache cache = CACHE_MANAGER.getCache(cacheNames);
        if (cache == null) {
            return;
        }
        cache.evict(key);
    }

    /**
     * 清空缓存值
     *
     * @param cacheNames 缓存组名称
     */
    public static void clear(String cacheNames) {
        Cache cache = CACHE_MANAGER.getCache(cacheNames);
        if (cache == null) {
            return;
        }
        cache.clear();
    }
}
