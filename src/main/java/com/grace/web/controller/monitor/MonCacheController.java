package com.grace.web.controller.monitor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.grace.common.constant.CacheConstants;
import com.grace.common.core.domain.AjaxResult;
import com.grace.common.utils.redis.CacheUtils;
import com.grace.common.utils.redis.RedisUtils;
import com.grace.web.system.domain.SysCache;
import lombok.RequiredArgsConstructor;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 缓存监控
 *
 * @author chanfa
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/cache")
public class MonCacheController {

    private final RedissonConnectionFactory connectionFactory;

    private final static List<SysCache> CACHES = new ArrayList<>();

    static {
        CACHES.add(new SysCache(CacheConstants.LOGIN_TOKEN_KEY, "在线用户"));
        CACHES.add(new SysCache(CacheConstants.SYS_CONFIG_KEY, "配置信息"));
        CACHES.add(new SysCache(CacheConstants.SYS_DICT_KEY, "数据字典"));
        CACHES.add(new SysCache(CacheConstants.CAPTCHA_CODE_KEY, "验证码"));
        CACHES.add(new SysCache(CacheConstants.REPEAT_SUBMIT_KEY, "防重提交"));
        CACHES.add(new SysCache(CacheConstants.RATE_LIMIT_KEY, "限流处理"));
        CACHES.add(new SysCache(CacheConstants.PWD_ERR_CNT_KEY, "密码错误次数"));
    }

    /**
     * 获取缓存监控统计信息
     *
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('monitor:cache:list')")
    @GetMapping()
    public AjaxResult getInfo() {
        RedisConnection connection = connectionFactory.getConnection();
        Properties info = connection.serverCommands().info();
        Properties commandStats = connection.serverCommands().info("commandStats");
        Long dbSize = connection.serverCommands().dbSize();

        List<Map<String, String>> pieList = new ArrayList<>();
        if (commandStats != null) {
            commandStats.stringPropertyNames().forEach(key -> {
                Map<String, String> data = new HashMap<>(2);
                String property = commandStats.getProperty(key);
                data.put("name", StrUtil.removePrefix(key, "cmdstat_"));
                data.put("value", StrUtil.subBetween(property, "calls=", ",usec"));
                pieList.add(data);
            });
        }

        // 返回
        Map<String, Object> result = new HashMap<>(3);
        result.put("info", info);
        result.put("dbSize", dbSize);
        result.put("commandStats", pieList);
        return AjaxResult.success(result);
    }

    /**
     * 获取缓存列表
     *
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('monitor:cache:list')")
    @GetMapping("/getNames")
    public AjaxResult cache() {
        return AjaxResult.success(CACHES);
    }

    /**
     * 根据缓存名称获取键名列表
     *
     * @param cacheName 缓存名称
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('monitor:cache:list')")
    @GetMapping("/getKeys/{cacheName}")
    public AjaxResult getCacheKeys(@PathVariable String cacheName) {
        Collection<String> cacheKeys = new HashSet<>(0);
        if (isCacheNames(cacheName)) {
            Set<Object> keys = CacheUtils.keys(cacheName);
            if (CollUtil.isNotEmpty(keys)) {
                cacheKeys = keys.stream().map(Object::toString).collect(Collectors.toList());
            }
        } else {
            cacheKeys = RedisUtils.keys(cacheName + "*");
        }
        return AjaxResult.success(cacheKeys);
    }

    /**
     * 根据缓存名称、缓存键名获取缓存内容
     *
     * @param cacheName 缓存名称
     * @param cacheKey  缓存键值
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('monitor:cache:list')")
    @GetMapping("/getValue/{cacheName}/{cacheKey}")
    public AjaxResult getCacheValue(@PathVariable String cacheName, @PathVariable String cacheKey) {
        Object cacheValue;
        if (isCacheNames(cacheName)) {
            cacheValue = CacheUtils.get(cacheName, cacheKey);
        } else {
            cacheValue = RedisUtils.getCacheObject(cacheKey);
        }
        SysCache sysCache = new SysCache(cacheName, cacheKey, JSONUtil.toJsonStr(cacheValue));
        return AjaxResult.success(sysCache);
    }

    /**
     * 根据缓存名称删除缓存信息
     *
     * @param cacheName 缓存名称
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('monitor:cache:list')")
    @DeleteMapping("/clearCacheName/{cacheName}")
    public AjaxResult clearCacheName(@PathVariable String cacheName) {
        if (isCacheNames(cacheName)) {
            CacheUtils.clear(cacheName);
        } else {
            RedisUtils.deleteKeys(cacheName + "*");
        }
        return AjaxResult.success();
    }

    /**
     * 根据缓存键名删除缓存信息
     *
     * @param cacheKey 缓存键值
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('monitor:cache:list')")
    @DeleteMapping("/clearCacheKey/{cacheName}/{cacheKey}")
    public AjaxResult clearCacheKey(@PathVariable String cacheName, @PathVariable String cacheKey) {
        if (isCacheNames(cacheName)) {
            CacheUtils.evict(cacheName, cacheKey);
        } else {
            RedisUtils.deleteObject(cacheKey);
        }
        return AjaxResult.success();
    }

    /**
     * 清空缓存信息
     *
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('monitor:cache:list')")
    @DeleteMapping("/clearCacheAll")
    public AjaxResult clearCacheAll() {
        RedisUtils.deleteKeys("*");
        return AjaxResult.success();
    }

    /**
     * 判断是否缓存名称
     *
     * @param cacheName 缓存名称
     * @return 结果
     */
    private boolean isCacheNames(String cacheName) {
        return !StrUtil.contains(cacheName, StrUtil.COLON);
    }
}
