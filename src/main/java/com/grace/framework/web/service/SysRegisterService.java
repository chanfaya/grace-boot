package com.grace.framework.web.service;

import cn.hutool.core.util.StrUtil;
import com.grace.common.constant.CacheConstants;
import com.grace.common.constant.Constants;
import com.grace.common.constant.UserConstants;
import com.grace.common.core.domain.RegisterBody;
import com.grace.common.exception.CustomException;
import com.grace.common.utils.SecurityUtils;
import com.grace.common.utils.redis.RedisUtils;
import com.grace.framework.manager.AsyncManager;
import com.grace.framework.manager.factory.AsyncFactory;
import com.grace.web.system.domain.SysUser;
import com.grace.web.system.service.ISysConfigService;
import com.grace.web.system.service.ISysUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 注册校验方法
 *
 * @author chanfa
 */
@Component
public class SysRegisterService {

    @Resource
    private ISysUserService userService;

    @Resource
    private ISysConfigService configService;

    /**
     * 注册
     */
    public String register(RegisterBody registerBody) {
        String msg = StrUtil.EMPTY, username = registerBody.getUsername(), password = registerBody.getPassword();
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);
        // 验证码开关
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled) {
            validateCaptcha(registerBody.getCode(), registerBody.getUuid());
        }
        if (StrUtil.isEmpty(username)) {
            msg = "用户名不能为空";
        } else if (StrUtil.isEmpty(password)) {
            msg = "密码不能为空";
        } else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            msg = "账户长度必须在2到20个字符之间";
        } else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            msg = "密码长度必须在5到20个字符之间";
        } else if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(sysUser))) {
            msg = "该用户名已存在，请重新输入";
        } else {
            sysUser.setNickName(username);
            sysUser.setPassword(SecurityUtils.encryptPassword(password));
            boolean regFlag = userService.registerUser(sysUser);
            if (!regFlag) {
                msg = "注册失败,请联系系统管理人员";
            } else {
                AsyncManager.me().execute(AsyncFactory.recordSyslog(username, Constants.REGISTER, "注册成功"));
            }
        }
        return msg;
    }

    /**
     * 校验验证码
     *
     * @param code 验证码
     * @param uuid 唯一标识
     */
    public void validateCaptcha(String code, String uuid) {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StrUtil.nullToDefault(uuid, StrUtil.EMPTY);
        String captcha = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        if (captcha == null) {
            throw new CustomException("验证码已失效");
        }
        if (!code.equalsIgnoreCase(captcha)) {
            throw new CustomException("验证码错误");
        }
    }
}
