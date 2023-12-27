package com.grace.web.controller.system;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.grace.common.annotation.Log;
import com.grace.common.constant.UserConstants;
import com.grace.common.core.controller.BaseController;
import com.grace.common.core.domain.AjaxResult;
import com.grace.common.enums.BusinessType;
import com.grace.web.system.domain.SysDictType;
import com.grace.web.system.service.ISysDictTypeService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 字典类型
 *
 * @author chanfa
 */
@RestController
@RequestMapping("/system/dict/type")
public class SysDictTypeController extends BaseController {

    @Resource
    private ISysDictTypeService dictTypeService;

    /**
     * 获取字典类型列表
     *
     * @param dictType 查询条件
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:dict:list')")
    @GetMapping("/list")
    public AjaxResult list(SysDictType dictType) {
        List<SysDictType> list = dictTypeService.selectDictTypeList(dictType);
        return AjaxResult.success(list);
    }

    /**
     * 导出字典类型列表
     *
     * @param dictType 查询条件
     */
    @Log(title = "字典类型", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPerm('system:dict:export')")
    @PostMapping("/export")
    public void export(SysDictType dictType) {
        List<SysDictType> list = dictTypeService.selectDictTypeList(dictType);
        Map<String, String> headAlias = new LinkedHashMap<>(4);
        headAlias.put("dictId", "字典类型ID");
        headAlias.put("dictName", "字典名称");
        headAlias.put("dictType", "字典类型");
        export(list, headAlias);
    }

    /**
     * 查询字典类型详细
     *
     * @param dictId 字典类型id
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:dict:query')")
    @GetMapping(value = "/{dictId}")
    public AjaxResult getInfo(@PathVariable Long dictId) {
        return AjaxResult.success(dictTypeService.getById(dictId));
    }

    /**
     * 新增字典类型
     *
     * @param dict 字典类型信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:dict:add')")
    @Log(title = "字典类型", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysDictType dict) {
        if (UserConstants.NOT_UNIQUE.equals(dictTypeService.checkDictTypeUnique(dict))) {
            return AjaxResult.error("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        return toAjax(dictTypeService.insertDictType(dict));
    }

    /**
     * 修改字典类型
     *
     * @param dict 字典类型信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:dict:edit')")
    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysDictType dict) {
        if (UserConstants.NOT_UNIQUE.equals(dictTypeService.checkDictTypeUnique(dict))) {
            return AjaxResult.error("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        return toAjax(dictTypeService.updateDictType(dict));
    }

    /**
     * 删除字典类型
     *
     * @param dictIds 字典类型id数组
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:dict:remove')")
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dictIds}")
    public AjaxResult remove(@PathVariable Long[] dictIds) {
        dictTypeService.deleteDictTypeByIds(dictIds);
        return success();
    }

    /**
     * 刷新字典缓存
     *
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:dict:remove')")
    @Log(title = "字典类型", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refreshCache")
    public AjaxResult refreshCache() {
        dictTypeService.resetDictCache();
        return AjaxResult.success();
    }

    /**
     * 获取字典选择框列表
     *
     * @return 结果
     */
    @GetMapping("/optionSelect")
    public AjaxResult optionSelect() {
        List<SysDictType> dictTypes = dictTypeService.list(Wrappers.emptyWrapper());
        return AjaxResult.success(dictTypes);
    }
}
