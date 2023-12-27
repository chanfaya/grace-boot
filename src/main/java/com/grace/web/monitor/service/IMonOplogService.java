package com.grace.web.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.grace.web.monitor.domain.MonOplog;

import java.util.List;

/**
 * IMonOpLogService
 *
 * @author chanfa
 */
public interface IMonOplogService extends IService<MonOplog> {

    /**
     * 查询系统操作日志集合
     *
     * @param monOplog 操作日志对象
     * @return 操作日志集合
     */
    List<MonOplog> selectOplogList(MonOplog monOplog);

    /**
     * 清空操作日志
     */
    void cleanOplog();
}
