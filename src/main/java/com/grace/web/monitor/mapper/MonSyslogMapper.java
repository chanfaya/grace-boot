package com.grace.web.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grace.web.monitor.domain.MonSyslog;
import org.apache.ibatis.annotations.Mapper;

/**
 * MonSyslogMapper
 *
 * @author chanfa
 */
@Mapper
public interface MonSyslogMapper extends BaseMapper<MonSyslog> {

    /**
     * 清空系统登录日志
     */
    void cleanSyslog();
}
