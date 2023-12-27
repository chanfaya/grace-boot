package com.grace.framework.manager.factory;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.grace.common.constant.Constants;
import com.grace.common.utils.CommonUtils;
import com.grace.common.utils.ServletUtils;
import com.grace.web.monitor.domain.MonOplog;
import com.grace.web.monitor.domain.MonSyslog;
import com.grace.web.monitor.service.IMonOplogService;
import com.grace.web.monitor.service.IMonSyslogService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.TimerTask;

/**
 * 异步工厂
 *
 * @author chanfa
 */
@Slf4j
public class AsyncFactory {

    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status   状态
     * @param message  消息
     * @param args     列表
     * @return 任务task
     */
    public static TimerTask recordSyslog(final String username, final String status, final String message, final Object... args) {
        final UserAgent userAgent = UserAgentUtil.parse(ServletUtils.getRequest().getHeader("User-Agent"));
        final String ip = ServletUtils.getClientIP(ServletUtils.getRequest());
        return new TimerTask() {
            @Override
            public void run() {
                // 查询地址
                String address = CommonUtils.getAddressByIp(ip);
                // 打印信息到日志
                String template = "[{}]";
                String s = StrUtil.format(template, ip) + address +
                        StrUtil.format(template, username) +
                        StrUtil.format(template, status) +
                        StrUtil.format(template, message);
                log.info(s, args);
                // 获取客户端操作系统
                String os = userAgent.getPlatform() + StrUtil.SPACE + userAgent.getOsVersion();
                // 获取客户端浏览器
                String browser = userAgent.getBrowser() + StrUtil.SPACE + userAgent.getVersion();
                // 封装对象
                MonSyslog monSyslog = new MonSyslog();
                monSyslog.setUserName(username);
                monSyslog.setIpaddr(ip);
                monSyslog.setLoginLocation(address);
                monSyslog.setBrowser(browser);
                monSyslog.setOs(os);
                monSyslog.setMsg(message);
                monSyslog.setLoginTime(LocalDateTime.now());
                // 日志状态
                if (StrUtil.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER)) {
                    monSyslog.setStatus(Constants.SUCCESS);
                } else if (Constants.LOGIN_FAIL.equals(status)) {
                    monSyslog.setStatus(Constants.FAIL);
                }
                // 插入数据
                SpringUtil.getBean(IMonSyslogService.class).save(monSyslog);
            }
        };
    }

    /**
     * 操作日志记录
     *
     * @param monOplog 操作日志信息
     * @return 任务task
     */
    public static TimerTask recordOplog(final MonOplog monOplog) {
        return new TimerTask() {
            @Override
            public void run() {
                // 远程查询操作地点
                monOplog.setOplogLocation(CommonUtils.getAddressByIp(monOplog.getOplogIp()));
                monOplog.setOplogTime(LocalDateTime.now());
                SpringUtil.getBean(IMonOplogService.class).save(monOplog);
            }
        };
    }
}
