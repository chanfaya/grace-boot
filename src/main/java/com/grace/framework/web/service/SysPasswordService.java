package com.grace.framework.web.service;

import cn.hutool.core.util.StrUtil;
import com.grace.common.constant.CacheConstants;
import com.grace.common.constant.Constants;
import com.grace.common.exception.CustomException;
import com.grace.common.utils.SecurityUtils;
import com.grace.common.utils.redis.RedisUtils;
import com.grace.framework.manager.AsyncManager;
import com.grace.framework.manager.factory.AsyncFactory;
import com.grace.framework.security.context.AuthenticationContextHolder;
import com.grace.web.system.domain.SysUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 登录密码方法
 *
 * @author chanfa
 */
@Component
public class SysPasswordService {

    /**
     * 密码最大错误次数（默认5次）
     */
    @Value("${user.password.maxRetryCount:5}")
    private int maxRetryCount;

    /**
     * 密码锁定时间（默认10分钟）
     */
    @Value("${user.password.lockTime:10}")
    private int lockTime;

    /**
     * 登录账户密码错误次数缓存键名
     *
     * @param username 用户名
     * @return 缓存键key
     */
    private String getCacheKey(String username) {
        return CacheConstants.PWD_ERR_CNT_KEY + username;
    }

    public void validate(SysUser user) {
        Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();

        // 获取缓存的错误次数
        Integer retryCount = RedisUtils.getCacheObject(getCacheKey(username));
        if (retryCount == null) {
            // 为空默认0
            retryCount = 0;
        }
        if (retryCount >= maxRetryCount) {
            // 错误次数达到阈值，锁定账户
            String msg = StrUtil.format("密码输入错误{}次，帐户锁定{}分钟", maxRetryCount, lockTime);
            AsyncManager.me().execute(AsyncFactory.recordSyslog(username, Constants.LOGIN_FAIL, msg));
            throw new CustomException(msg);
        }

        // 校验密码
        boolean matches = SecurityUtils.matchesPassword(password, user.getPassword());
        if (matches) {
            // 清除缓存
            clearLoginRecordCache(username);
        } else {
            // 密码错误：错误次数+1，更新缓存
            retryCount = retryCount + 1;
            String msg = StrUtil.format("密码输入错误{}次", retryCount);
            AsyncManager.me().execute(AsyncFactory.recordSyslog(username, Constants.LOGIN_FAIL, msg));
            RedisUtils.setCacheObject(getCacheKey(username), retryCount, Duration.ofMinutes(lockTime));
            throw new CustomException(msg);
        }
    }

    /**
     * 清楚登录记录缓存
     *
     * @param loginName 用户名
     */
    public void clearLoginRecordCache(String loginName) {
        if (RedisUtils.hasKey(getCacheKey(loginName))) {
            RedisUtils.deleteObject(getCacheKey(loginName));
        }
    }
}
