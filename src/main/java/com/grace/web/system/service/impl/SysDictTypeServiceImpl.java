package com.grace.web.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grace.common.constant.UserConstants;
import com.grace.common.exception.CustomException;
import com.grace.common.utils.DictUtils;
import com.grace.web.system.domain.SysDictData;
import com.grace.web.system.domain.SysDictType;
import com.grace.web.system.mapper.SysDictDataMapper;
import com.grace.web.system.mapper.SysDictTypeMapper;
import com.grace.web.system.service.ISysDictTypeService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SysDictTypeServiceImpl
 *
 * @author chanfa
 */
@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {

    @Resource
    private SysDictTypeMapper dictTypeMapper;

    @Resource
    private SysDictDataMapper dictDataMapper;

    /**
     * 项目启动时，初始化字典到缓存
     */
    @PostConstruct
    public void init() {
        loadingDictCache();
    }

    /**
     * 根据条件分页查询字典类型
     *
     * @param dictType 字典类型信息
     * @return 字典类型集合信息
     */
    @Override
    public List<SysDictType> selectDictTypeList(SysDictType dictType) {
        QueryWrapper<SysDictType> wrapper = Wrappers.query(dictType);
        Object beginTime = dictType.getParams().get("beginTime");
        Object endTime = dictType.getParams().get("endTime");
        if (ObjectUtil.isNotNull(beginTime) && ObjectUtil.isNotNull(endTime)) {
            wrapper.between("date_format(login_time, '%Y-%m-%d')", beginTime, endTime);
        }
        return dictTypeMapper.selectList(wrapper);
    }

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        List<SysDictData> dictDataList = DictUtils.getDictCache(dictType);
        if (CollUtil.isNotEmpty(dictDataList)) {
            return dictDataList;
        }
        dictDataList = dictDataMapper.selectDictDataByType(dictType);
        if (CollUtil.isNotEmpty(dictDataList)) {
            DictUtils.setDictCache(dictType, dictDataList);
            return dictDataList;
        }
        return null;
    }

    /**
     * 批量删除字典类型信息
     *
     * @param dictIds 需要删除的字典ID
     */
    @Override
    public void deleteDictTypeByIds(Long[] dictIds) {
        for (Long dictId : dictIds) {
            SysDictType dictType = dictTypeMapper.selectById(dictId);
            LambdaQueryWrapper<SysDictData> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(SysDictData::getDictType, dictType.getDictType());
            if (dictDataMapper.selectCount(wrapper) > 0) {
                throw new CustomException(String.format("%1$s已分配,不能删除", dictType.getDictName()));
            }
            dictTypeMapper.deleteById(dictId);
            DictUtils.removeDictCache(dictType.getDictType());
        }
    }

    /**
     * 加载字典缓存数据
     */
    public void loadingDictCache() {
        LambdaQueryWrapper<SysDictData> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDictData::getStatus, "0").orderByAsc(SysDictData::getDictSort);
        Map<String, List<SysDictData>> dictDataMap = dictDataMapper.selectList(wrapper).stream().collect(Collectors.groupingBy(SysDictData::getDictType));
        for (Map.Entry<String, List<SysDictData>> entry : dictDataMap.entrySet()) {
            DictUtils.setDictCache(entry.getKey(), entry.getValue().stream().sorted(Comparator.comparing(SysDictData::getDictSort)).collect(Collectors.toList()));
        }
    }

    /**
     * 清空字典缓存数据
     */
    public void clearDictCache() {
        DictUtils.clearDictCache();
    }

    /**
     * 重置字典缓存数据
     */
    @Override
    public void resetDictCache() {
        clearDictCache();
        loadingDictCache();
    }

    /**
     * 新增保存字典类型信息
     *
     * @param dict 字典类型信息
     * @return 结果
     */
    @Override
    public int insertDictType(SysDictType dict) {
        int row = dictTypeMapper.insert(dict);
        if (row > 0) {
            DictUtils.setDictCache(dict.getDictType(), null);
        }
        return row;
    }

    /**
     * 修改保存字典类型信息
     *
     * @param dict 字典类型信息
     * @return 结果
     */
    @Override
    public int updateDictType(SysDictType dict) {
        SysDictType oldDict = dictTypeMapper.selectById(dict.getDictId());
        UpdateWrapper<SysDictData> wrapper = Wrappers.update();
        wrapper.set("dict_type", dict.getDictType()).eq("dict_type", oldDict.getDictType());
        int row = dictDataMapper.update(wrapper);
        if (row > 0) {
            List<SysDictData> dictDataList = dictDataMapper.selectDictDataByType(dict.getDictType());
            DictUtils.setDictCache(dict.getDictType(), dictDataList);
        }
        return row;
    }

    /**
     * 校验字典类型称是否唯一
     *
     * @param dict 字典类型
     * @return 结果
     */
    @Override
    public String checkDictTypeUnique(SysDictType dict) {
        LambdaQueryWrapper<SysDictType> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDictType::getDictType, dict.getDictType());
        if (ObjectUtil.isNotNull(dict.getDictId())) {
            wrapper.ne(SysDictType::getDictId, dict.getDictId());
        }
        if (dictTypeMapper.selectCount(wrapper) > 0) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}
