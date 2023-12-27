package com.grace.web.monitor.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grace.web.monitor.domain.MonOplog;
import com.grace.web.monitor.mapper.MonOplogMapper;
import com.grace.web.monitor.service.IMonOplogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MonOpLogServiceImpl
 *
 * @author chanfa
 */
@Service
public class MonOplogServiceImpl extends ServiceImpl<MonOplogMapper, MonOplog> implements IMonOplogService {

    @Resource
    private MonOplogMapper monOplogMapper;

    /**
     * 查询系统操作日志集合
     *
     * @param monOplog 操作日志对象
     * @return 操作日志集合
     */
    @Override
    public List<MonOplog> selectOplogList(MonOplog monOplog) {
        QueryWrapper<MonOplog> wrapper = Wrappers.query(monOplog);
        Object beginTime = monOplog.getParams().get("beginTime");
        Object endTime = monOplog.getParams().get("endTime");
        if (ObjectUtil.isNotNull(beginTime) && ObjectUtil.isNotNull(endTime)) {
            wrapper.between("date_format(oplog_time, '%Y-%m-%d')", beginTime, endTime);
        }
        wrapper.lambda().orderByDesc(MonOplog::getOplogId);
        return monOplogMapper.selectList(wrapper);
    }

    /**
     * 清空操作日志
     */
    @Override
    public void cleanOplog() {
        monOplogMapper.cleanOplog();
    }
}
