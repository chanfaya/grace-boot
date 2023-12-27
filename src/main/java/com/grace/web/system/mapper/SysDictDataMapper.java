package com.grace.web.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grace.web.system.domain.SysDictData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * SysDictDataMapper
 *
 * @author chanfa
 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    List<SysDictData> selectDictDataByType(String dictType);
}
