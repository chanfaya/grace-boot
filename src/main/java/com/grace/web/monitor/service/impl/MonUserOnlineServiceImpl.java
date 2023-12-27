package com.grace.web.monitor.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.grace.common.core.domain.LoginUser;
import com.grace.web.monitor.domain.MonUserOnline;
import com.grace.web.monitor.service.IMonUserOnlineService;
import org.springframework.stereotype.Service;

/**
 * SysUserOnlineServiceImpl
 *
 * @author chanfa
 */
@Service
public class MonUserOnlineServiceImpl implements IMonUserOnlineService {

    /**
     * 通过登录地址查询信息
     *
     * @param ipaddr 登录地址
     * @param user   用户信息
     * @return 在线用户信息
     */
    @Override
    public MonUserOnline selectOnlineByIpaddr(String ipaddr, LoginUser user) {
        if (StrUtil.equals(ipaddr, user.getIpaddr())) {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     * 通过用户名称查询信息
     *
     * @param userName 用户名称
     * @param user     用户信息
     * @return 在线用户信息
     */
    @Override
    public MonUserOnline selectOnlineByUserName(String userName, LoginUser user) {
        if (StrUtil.equals(userName, user.getUsername())) {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     * 通过登录地址/用户名称查询信息
     *
     * @param ipaddr   登录地址
     * @param userName 用户名称
     * @param user     用户信息
     * @return 在线用户信息
     */
    @Override
    public MonUserOnline selectOnlineByInfo(String ipaddr, String userName, LoginUser user) {
        if (StrUtil.equals(ipaddr, user.getIpaddr()) && StrUtil.equals(userName, user.getUsername())) {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     * 设置在线用户信息
     *
     * @param user 用户信息
     * @return 在线用户
     */
    @Override
    public MonUserOnline loginUserToUserOnline(LoginUser user) {
        if (ObjectUtil.isNull(user) || ObjectUtil.isNull(user.getUser())) {
            return null;
        }
        MonUserOnline monUserOnline = new MonUserOnline();
        monUserOnline.setTokenId(user.getToken());
        monUserOnline.setUserName(user.getUsername());
        monUserOnline.setIpaddr(user.getIpaddr());
        monUserOnline.setLoginLocation(user.getLoginLocation());
        monUserOnline.setBrowser(user.getBrowser());
        monUserOnline.setOs(user.getOs());
        monUserOnline.setLoginTime(user.getLoginTime());
        if (ObjectUtil.isNotNull(user.getUser().getDept())) {
            monUserOnline.setDeptName(user.getUser().getDept().getDeptName());
        }
        return monUserOnline;
    }
}
