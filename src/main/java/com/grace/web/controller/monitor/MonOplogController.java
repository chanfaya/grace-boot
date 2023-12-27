package com.grace.web.controller.monitor;

import com.grace.common.annotation.Log;
import com.grace.common.core.controller.BaseController;
import com.grace.common.core.domain.AjaxResult;
import com.grace.common.enums.BusinessType;
import com.grace.web.monitor.domain.MonOplog;
import com.grace.web.monitor.service.IMonOplogService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志记录
 *
 * @author chanfa
 */
@RestController
@RequestMapping("/monitor/oplog")
public class MonOplogController extends BaseController {

    @Resource
    private IMonOplogService monOpLogService;

    /**
     * 获取操作日志列表
     *
     * @param monOplog 查询条件
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('monitor:oplog:list')")
    @GetMapping("/list")
    public AjaxResult list(MonOplog monOplog) {
        List<MonOplog> list = monOpLogService.selectOplogList(monOplog);
        return AjaxResult.success(list);
    }

    /**
     * 导出操作日志
     *
     * @param monOplog 查询条件
     */
    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPerm('monitor:oplog:export')")
    @PostMapping("/export")
    public void export(MonOplog monOplog) {
        List<MonOplog> list = monOpLogService.selectOplogList(monOplog);
        Map<String, String> headAlias = new LinkedHashMap<>(32);
        headAlias.put("oplogId", "序号");
        headAlias.put("title", "操作模块");
        headAlias.put("businessType", "业务类型");
        headAlias.put("method", "请求方法");
        headAlias.put("requestMethod", "请求方式");
        headAlias.put("operatorType", "操作类别");
        headAlias.put("oplogName", "操作人员");
        headAlias.put("deptName", "部门名称");
        headAlias.put("oplogUrl", "请求url");
        headAlias.put("oplogIp", "操作地址");
        headAlias.put("oplogLocation", "操作地点");
        headAlias.put("oplogParam", "请求参数");
        headAlias.put("jsonResult", "返回参数");
        headAlias.put("status", "操作状态");
        headAlias.put("errorMsg", "错误消息");
        headAlias.put("oplogTime", "操作时间");
        export(list, headAlias);
    }

    /**
     * 删除操作日志
     *
     * @param oplogIds 日志id数组
     * @return 结果
     */
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPerm('monitor:oplog:remove')")
    @DeleteMapping("/{oplogIds}")
    public AjaxResult remove(@PathVariable Long[] oplogIds) {
        return toAjax(monOpLogService.removeByIds(Arrays.asList(oplogIds)));
    }

    /**
     * 清空日志
     *
     * @return 结果
     */
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @PreAuthorize("@ss.hasPerm('monitor:oplog:remove')")
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        monOpLogService.cleanOplog();
        return AjaxResult.success();
    }
}
