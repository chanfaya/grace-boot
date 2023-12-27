package com.grace.framework.web.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.grace.common.core.domain.LoginUser;
import com.grace.common.utils.SecurityUtils;
import com.grace.framework.security.context.PermissionContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 自定义权限实现
 *
 * @author chanfa
 */
@Service("ss")
public class PermissionService {

    /**
     * 所有权限标识
     */
    public static final String ALL_PERMISSION = "*:*:*";

    /**
     * 验证用户是否具备某权限
     *
     * @param permission 权限字符串
     * @return 用户是否具备某权限
     */
    public boolean hasPerm(String permission) {
        if (StrUtil.isEmpty(permission)) {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (ObjectUtil.isNull(loginUser)) {
            return false;
        }
        // 用户权限集合
        Set<String> permissions = loginUser.getPermissions();
        if (CollUtil.isEmpty(permissions)) {
            return false;
        }
        PermissionContextHolder.setContext(permission);
        // 判断是否包含指定权限
        return permissions.contains(ALL_PERMISSION) || permissions.contains(permission.trim());
    }
}
