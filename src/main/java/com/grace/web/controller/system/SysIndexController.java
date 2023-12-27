package com.grace.web.controller.system;

import cn.hutool.core.util.StrUtil;
import com.grace.common.config.CommonConfig;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页
 *
 * @author chanfa
 */
@RestController
public class SysIndexController {

    /**
     * 首页
     *
     * @return 结果
     */
    @RequestMapping("/")
    public String index() {
        return StrUtil.format("欢迎使用{}后台管理框架，请通过前端地址访问。", CommonConfig.getName());
    }
}
