package com.grace.web.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.grace.web.monitor.domain.MonSyslog;

import java.util.List;

/**
 * IMonSyslogService
 *
 * @author chanfa
 */
public interface IMonSyslogService extends IService<MonSyslog> {

    /**
     * 查询系统登录日志集合
     *
     * @param monSyslog 访问日志对象
     * @return 登录记录集合
     */
    List<MonSyslog> selectSyslogList(MonSyslog monSyslog);

    /**
     * 清空系统登录日志
     */
    void cleanSyslog();
}
