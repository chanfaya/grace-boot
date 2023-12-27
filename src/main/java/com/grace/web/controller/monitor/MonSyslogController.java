package com.grace.web.controller.monitor;

import com.grace.common.annotation.Log;
import com.grace.common.core.controller.BaseController;
import com.grace.common.core.domain.AjaxResult;
import com.grace.common.enums.BusinessType;
import com.grace.framework.web.service.SysPasswordService;
import com.grace.web.monitor.domain.MonSyslog;
import com.grace.web.monitor.service.IMonSyslogService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统访问记录
 *
 * @author chanfa
 */
@RestController
@RequestMapping("/monitor/syslog")
public class MonSyslogController extends BaseController {

    @Resource
    private IMonSyslogService monSyslogService;

    @Resource
    private SysPasswordService passwordService;

    /**
     * 获取登录日志列表
     *
     * @param monSyslog 查询条件
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('monitor:syslog:list')")
    @GetMapping("/list")
    public AjaxResult list(MonSyslog monSyslog) {
        List<MonSyslog> list = monSyslogService.selectSyslogList(monSyslog);
        return AjaxResult.success(list);
    }

    /**
     * 导出登录日志
     *
     * @param monSyslog 查询条件
     */
    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPerm('monitor:syslog:export')")
    @PostMapping("/export")
    public void export(MonSyslog monSyslog) {
        List<MonSyslog> list = monSyslogService.selectSyslogList(monSyslog);
        Map<String, String> headAlias = new LinkedHashMap<>(16);
        headAlias.put("syslogId", "序号");
        headAlias.put("userName", "用户账号");
        headAlias.put("status", "登录状态");
        headAlias.put("ipaddr", "登录IP地址");
        headAlias.put("loginLocation", "登陆地点");
        headAlias.put("browser", "浏览器类型");
        headAlias.put("os", "操作系统");
        headAlias.put("msg", "提示消息");
        headAlias.put("loginTime", "访问时间");
        export(list, headAlias);
    }

    /**
     * 删除登录日志
     *
     * @param syslogIds 日志id数组
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('monitor:syslog:remove')")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{syslogIds}")
    public AjaxResult remove(@PathVariable Long[] syslogIds) {
        return toAjax(monSyslogService.removeByIds(Arrays.asList(syslogIds)));
    }

    /**
     * 清空日志
     *
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('monitor:syslog:remove')")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        monSyslogService.cleanSyslog();
        return success();
    }

    /**
     * 账户解锁
     *
     * @param userName 用户名
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('monitor:logininfor:unlock')")
    @Log(title = "账户解锁", businessType = BusinessType.OTHER)
    @GetMapping("/unlock/{userName}")
    public AjaxResult unlock(@PathVariable("userName") String userName) {
        passwordService.clearLoginRecordCache(userName);
        return success();
    }
}
