package com.grace.framework.web.service;

import cn.hutool.core.util.StrUtil;
import com.grace.common.constant.CacheConstants;
import com.grace.common.constant.Constants;
import com.grace.common.core.domain.LoginUser;
import com.grace.common.exception.CustomException;
import com.grace.common.utils.ServletUtils;
import com.grace.common.utils.redis.RedisUtils;
import com.grace.framework.manager.AsyncManager;
import com.grace.framework.manager.factory.AsyncFactory;
import com.grace.framework.security.context.AuthenticationContextHolder;
import com.grace.web.system.domain.SysUser;
import com.grace.web.system.service.ISysConfigService;
import com.grace.web.system.service.ISysUserService;
import jakarta.annotation.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 登录校验方法
 *
 * @author chanfa
 */
@Component
public class SysLoginService {

    @Resource
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private ISysUserService userService;

    @Resource
    private ISysConfigService configService;

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid) {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        // 验证码开关
        if (captchaEnabled) {
            validateCaptcha(username, code, uuid);
        }
        // 用户验证
        Authentication authentication;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            String msg;
            if (e instanceof BadCredentialsException) {
                msg = "用户名或密码错误";
            } else {
                msg = e.getMessage();
            }
            AsyncManager.me().execute(AsyncFactory.recordSyslog(username, Constants.LOGIN_FAIL, msg));
            throw new CustomException(msg);
        } finally {
            AuthenticationContextHolder.clearContext();
        }
        AsyncManager.me().execute(AsyncFactory.recordSyslog(username, Constants.LOGIN_SUCCESS, "登陆成功"));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        recordSyslog(loginUser.getUserId());
        // 生成token
        return tokenService.createToken(loginUser);
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     */
    public void validateCaptcha(String username, String code, String uuid) {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StrUtil.nullToDefault(uuid, StrUtil.EMPTY);
        String captcha = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        if (captcha == null) {
            String msg = "验证码已失效";
            AsyncManager.me().execute(AsyncFactory.recordSyslog(username, Constants.LOGIN_FAIL, msg));
            throw new CustomException(msg);
        }
        if (!code.equalsIgnoreCase(captcha)) {
            String msg = "验证码错误";
            AsyncManager.me().execute(AsyncFactory.recordSyslog(username, Constants.LOGIN_FAIL, msg));
            throw new CustomException(msg);
        }
    }

    /**
     * 记录登录信息
     */
    public void recordSyslog(Long userId) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(ServletUtils.getClientIP(ServletUtils.getRequest()));
        sysUser.setLoginDate(LocalDateTime.now());
        userService.updateUserProfile(sysUser);
    }
}
