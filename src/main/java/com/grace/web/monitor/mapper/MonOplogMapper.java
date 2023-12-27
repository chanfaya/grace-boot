package com.grace.web.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grace.web.monitor.domain.MonOplog;
import org.apache.ibatis.annotations.Mapper;

/**
 * MonOplogMapper
 *
 * @author chanfa
 */
@Mapper
public interface MonOplogMapper extends BaseMapper<MonOplog> {

    /**
     * 清空操作日志
     */
    void cleanOplog();
}
