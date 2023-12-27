package com.grace.web.controller.system;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.grace.common.annotation.Log;
import com.grace.common.config.CommonConfig;
import com.grace.common.constant.Constants;
import com.grace.common.constant.UserConstants;
import com.grace.common.core.controller.BaseController;
import com.grace.common.core.domain.AjaxResult;
import com.grace.common.core.domain.LoginUser;
import com.grace.common.enums.BusinessType;
import com.grace.common.utils.SecurityUtils;
import com.grace.framework.web.service.TokenService;
import com.grace.web.system.domain.SysUser;
import com.grace.web.system.service.ISysUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 个人中心
 *
 * @author chanfa
 */
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController {

    @Resource
    private ISysUserService userService;

    @Resource
    private TokenService tokenService;

    /**
     * 获取个人信息
     *
     * @return 结果
     */
    @GetMapping
    public AjaxResult profile() {
        LoginUser loginUser = getLoginUser();
        SysUser user = loginUser.getUser();
        AjaxResult ajax = AjaxResult.success(user);
        ajax.put("roleGroup", userService.selectUserRoleGroup(loginUser.getUsername()));
        ajax.put("postGroup", userService.selectUserPostGroup(loginUser.getUsername()));
        return ajax;
    }

    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateProfile(@RequestBody SysUser user) {
        if (StrUtil.isNotEmpty(user.getPhoneNumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            return AjaxResult.error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        if (StrUtil.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            return AjaxResult.error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        LoginUser loginUser = getLoginUser();
        SysUser sysUser = loginUser.getUser();
        user.setUserName(sysUser.getUserName());
        user.setUserId(sysUser.getUserId());
        // 防止修改密码
        user.setPassword(null);
        // 防止修改头像
        user.setAvatar(null);
        // 防止修改部门
        user.setDeptId(null);
        if (userService.updateUserProfile(user) > 0) {
            // 更新缓存用户信息
            sysUser.setNickName(user.getNickName());
            sysUser.setPhoneNumber(user.getPhoneNumber());
            sysUser.setEmail(user.getEmail());
            sysUser.setSex(user.getSex());
            tokenService.setLoginUser(loginUser);
            return AjaxResult.success();
        }
        return AjaxResult.error("修改个人信息异常，请联系管理员");
    }

    /**
     * 修改密码
     *
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 结果
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE, isSaveRequestData = false)
    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(String oldPassword, String newPassword) {
        LoginUser loginUser = getLoginUser();
        String userName = loginUser.getUsername();
        String password = loginUser.getPassword();
        if (!SecurityUtils.matchesPassword(oldPassword, password)) {
            return AjaxResult.error("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, password)) {
            return AjaxResult.error("新密码不能与旧密码相同");
        }
        // 密码加密
        String encryptPassword = SecurityUtils.encryptPassword(newPassword);
        if (userService.resetUserPwd(userName, encryptPassword) > 0) {
            // 更新缓存用户密码
            loginUser.getUser().setPassword(encryptPassword);
            tokenService.setLoginUser(loginUser);
            return AjaxResult.success();
        }
        return AjaxResult.error("修改密码异常，请联系管理员");
    }

    /**
     * 头像上传
     *
     * @param file 头像文件
     * @return 结果
     * @throws IOException 异常
     */
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam("avatarFile") MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            LoginUser loginUser = getLoginUser();
            // 头像文件路径
            String filePath = CommonConfig.getAvatarPath();
            // 获取后缀名
            String suffix = StrUtil.DOT + FileTypeUtil.getType(file.getInputStream());
            // 重命名文件
            String fileName = SecurityUtils.getUserId() + suffix;
            // 写入磁盘
            FileUtil.writeFromStream(file.getInputStream(), filePath + fileName);
            // 获取相对资源路径
            String avatar = Constants.RESOURCE_PREFIX + CommonConfig.AVATAR + fileName;
            // 更新数据库
            if (userService.updateUserAvatar(getUsername(), avatar)) {
                AjaxResult ajax = AjaxResult.success();
                ajax.put("imgUrl", avatar);
                // 更新缓存用户头像
                loginUser.getUser().setAvatar(avatar);
                tokenService.setLoginUser(loginUser);
                return ajax;
            }
        }
        return AjaxResult.error("上传图片异常，请联系管理员");
    }
}
