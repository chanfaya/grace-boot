package com.grace.web.controller.monitor;

import com.grace.common.core.domain.AjaxResult;
import com.grace.web.monitor.domain.Server;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控
 *
 * @author chanfa
 */
@RestController
@RequestMapping("/monitor/server")
public class MonServerController {

    /**
     * 获取服务器监控信息
     *
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('monitor:server:list')")
    @GetMapping()
    public AjaxResult getInfo() {
        Server server = new Server();
        server.copyTo();
        return AjaxResult.success(server);
    }
}
