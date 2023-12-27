package com.grace.web.controller.system;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.grace.common.core.controller.BaseController;
import com.grace.common.core.domain.AjaxResult;
import com.grace.common.core.domain.RegisterBody;
import com.grace.framework.web.service.SysRegisterService;
import com.grace.web.system.service.ISysConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册
 *
 * @author chanfa
 */
@RestController
public class SysRegisterController extends BaseController {

    @Resource
    private SysRegisterService registerService;

    @Resource
    private ISysConfigService configService;

    /**
     * 注册
     *
     * @param user 注册信息
     * @return 结果
     */
    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user) {
        String key = "sys.account.registerUser";
        String config = configService.selectConfigByKey(key);
        if (!BooleanUtil.toBoolean(config)) {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StrUtil.isEmpty(msg) ? success() : error(msg);
    }
}
