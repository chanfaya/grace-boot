package com.grace.web.controller.system;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.grace.common.annotation.Log;
import com.grace.common.core.controller.BaseController;
import com.grace.common.core.domain.AjaxResult;
import com.grace.common.enums.BusinessType;
import com.grace.web.system.domain.SysNotice;
import com.grace.web.system.service.ISysNoticeService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 通知公告
 *
 * @author chanfa
 */
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseController {

    @Resource
    private ISysNoticeService noticeService;

    /**
     * 获取通知公告列表
     *
     * @param notice 查询条件
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:notice:list')")
    @GetMapping("/list")
    public AjaxResult list(SysNotice notice) {
        List<SysNotice> list = noticeService.list(Wrappers.query(notice));
        return AjaxResult.success(list);
    }

    /**
     * 根据通知公告id获取详细信息
     *
     * @param noticeId 通知公告id
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable Long noticeId) {
        return AjaxResult.success(noticeService.getById(noticeId));
    }

    /**
     * 新增通知公告
     *
     * @param notice 通知公告
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:notice:add')")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysNotice notice) {
        return toAjax(noticeService.save(notice));
    }

    /**
     * 修改通知公告
     *
     * @param notice 通知公告
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:notice:edit')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysNotice notice) {
        return toAjax(noticeService.updateById(notice));
    }

    /**
     * 删除通知公告
     *
     * @param noticeIds 通知公告id数组
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:notice:remove')")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds) {
        return toAjax(noticeService.removeByIds(Arrays.asList(noticeIds)));
    }
}
