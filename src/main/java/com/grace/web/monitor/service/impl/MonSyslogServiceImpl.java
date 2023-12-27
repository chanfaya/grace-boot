package com.grace.web.monitor.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grace.web.monitor.domain.MonSyslog;
import com.grace.web.monitor.mapper.MonSyslogMapper;
import com.grace.web.monitor.service.IMonSyslogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MonSyslogServiceImpl
 *
 * @author chanfa
 */
@Service
public class MonSyslogServiceImpl extends ServiceImpl<MonSyslogMapper, MonSyslog> implements IMonSyslogService {

    @Resource
    private MonSyslogMapper monSyslogMapper;

    /**
     * 查询系统登录日志集合
     *
     * @param monSyslog 访问日志对象
     * @return 登录记录集合
     */
    @Override
    public List<MonSyslog> selectSyslogList(MonSyslog monSyslog) {
        QueryWrapper<MonSyslog> wrapper = Wrappers.query(monSyslog);
        Object beginTime = monSyslog.getParams().get("beginTime");
        Object endTime = monSyslog.getParams().get("endTime");
        if (ObjectUtil.isNotNull(beginTime) && ObjectUtil.isNotNull(endTime)) {
            wrapper.between("date_format(login_time, '%Y-%m-%d')", beginTime, endTime);
        }
        wrapper.lambda().orderByDesc(MonSyslog::getSyslogId);
        return monSyslogMapper.selectList(wrapper);
    }

    /**
     * 清空系统登录日志
     */
    @Override
    public void cleanSyslog() {
        monSyslogMapper.cleanSyslog();
    }
}
