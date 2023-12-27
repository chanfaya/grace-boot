package com.grace.framework.security.handle;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import com.grace.common.constant.Constants;
import com.grace.common.core.domain.AjaxResult;
import com.grace.common.core.domain.LoginUser;
import com.grace.common.utils.ServletUtils;
import com.grace.framework.manager.AsyncManager;
import com.grace.framework.manager.factory.AsyncFactory;
import com.grace.framework.web.service.TokenService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * 退出成功处理类
 *
 * @author chanfa
 */
@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    @Resource
    private TokenService tokenService;

    /**
     * 退出成功
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (ObjectUtil.isNotNull(loginUser)) {
            String userName = loginUser.getUsername();
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
            // 记录用户退出日志
            AsyncManager.me().execute(AsyncFactory.recordSyslog(userName, Constants.LOGOUT, "退出成功"));
        }
        ServletUtils.write(response, JSONUtil.toJsonStr(AjaxResult.success("退出成功")), ContentType.build(ContentType.JSON, CharsetUtil.CHARSET_UTF_8));
    }
}
