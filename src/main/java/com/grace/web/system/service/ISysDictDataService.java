package com.grace.web.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.grace.web.system.domain.SysDictData;

/**
 * ISysDictDataService
 *
 * @author chanfa
 */
public interface ISysDictDataService extends IService<SysDictData> {

    /**
     * 批量删除字典数据信息
     *
     * @param dictCodes 需要删除的字典数据ID
     */
    void deleteDictDataByIds(Long[] dictCodes);

    /**
     * 新增保存字典数据信息
     *
     * @param dictData 字典数据信息
     * @return 结果
     */
    int insertDictData(SysDictData dictData);

    /**
     * 修改保存字典数据信息
     *
     * @param dictData 字典数据信息
     * @return 结果
     */
    int updateDictData(SysDictData dictData);
}
