package com.grace.web.controller.monitor;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.grace.common.annotation.Log;
import com.grace.common.constant.CacheConstants;
import com.grace.common.core.controller.BaseController;
import com.grace.common.core.domain.AjaxResult;
import com.grace.common.core.domain.LoginUser;
import com.grace.common.enums.BusinessType;
import com.grace.common.utils.redis.RedisUtils;
import com.grace.web.monitor.domain.MonUserOnline;
import com.grace.web.monitor.service.IMonUserOnlineService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 在线用户监控
 *
 * @author chanfa
 */
@RestController
@RequestMapping("/monitor/online")
public class MonUserOnlineController extends BaseController {

    @Resource
    private IMonUserOnlineService userOnlineService;

    /**
     * 获取在线用户列表
     *
     * @param ipaddr   登录ip
     * @param userName 用户名
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('monitor:online:list')")
    @GetMapping("/list")
    public AjaxResult list(String ipaddr, String userName) {
        Collection<String> keys = RedisUtils.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        List<MonUserOnline> userOnlineList = new ArrayList<>();
        for (String key : keys) {
            LoginUser user = RedisUtils.getCacheObject(key);
            if (StrUtil.isNotEmpty(ipaddr) && StrUtil.isNotEmpty(userName)) {
                if (StrUtil.equals(ipaddr, user.getIpaddr()) && StrUtil.equals(userName, user.getUsername())) {
                    userOnlineList.add(userOnlineService.selectOnlineByInfo(ipaddr, userName, user));
                }
            } else if (StrUtil.isNotEmpty(ipaddr)) {
                if (StrUtil.equals(ipaddr, user.getIpaddr())) {
                    userOnlineList.add(userOnlineService.selectOnlineByIpaddr(ipaddr, user));
                }
            } else if (StrUtil.isNotEmpty(userName) && ObjectUtil.isNotNull(user.getUser())) {
                if (StrUtil.equals(userName, user.getUsername())) {
                    userOnlineList.add(userOnlineService.selectOnlineByUserName(userName, user));
                }
            } else {
                userOnlineList.add(userOnlineService.loginUserToUserOnline(user));
            }
        }
        Collections.reverse(userOnlineList);
        userOnlineList.removeAll(Collections.singleton(null));
        return AjaxResult.success(userOnlineList);
    }

    /**
     * 强退用户
     *
     * @param tokenId tokenId
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('monitor:online:forceLogout')")
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @DeleteMapping("/{tokenId}")
    public AjaxResult forceLogout(@PathVariable String tokenId) {
        RedisUtils.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + tokenId);
        return AjaxResult.success();
    }
}
