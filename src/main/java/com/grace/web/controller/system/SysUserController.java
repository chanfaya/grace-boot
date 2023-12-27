package com.grace.web.controller.system;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelUtil;
import com.grace.common.annotation.Log;
import com.grace.common.constant.UserConstants;
import com.grace.common.core.controller.BaseController;
import com.grace.common.core.domain.AjaxResult;
import com.grace.common.enums.BusinessType;
import com.grace.common.utils.SecurityUtils;
import com.grace.web.system.domain.SysDept;
import com.grace.web.system.domain.SysRole;
import com.grace.web.system.domain.SysUser;
import com.grace.web.system.service.ISysDeptService;
import com.grace.web.system.service.ISysPostService;
import com.grace.web.system.service.ISysRoleService;
import com.grace.web.system.service.ISysUserService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理
 *
 * @author chanfa
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {

    @Resource
    private ISysUserService userService;

    @Resource
    private ISysRoleService roleService;

    @Resource
    private ISysDeptService deptService;

    @Resource
    private ISysPostService postService;

    /**
     * 获取用户列表
     *
     * @param user 查询条件
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:user:list')")
    @GetMapping("/list")
    public AjaxResult list(SysUser user) {
        List<SysUser> list = userService.selectUserList(user);
        return AjaxResult.success(list);
    }

    /**
     * 导出用户信息
     *
     * @param user 查询条件
     */
    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPerm('system:user:export')")
    @PostMapping("/export")
    public void export(SysUser user) {
        List<SysUser> list = userService.selectUserList(user);
        Map<String, String> headAlias = new LinkedHashMap<>(16);
        headAlias.put("userId", "用户ID");
        headAlias.put("userName", "用户账号");
        headAlias.put("nickName", "用户昵称");
        headAlias.put("email", "用户邮箱");
        headAlias.put("phoneNumber", "手机号码");
        headAlias.put("sex", "用户性别");
        headAlias.put("loginIp", "最后登录IP");
        headAlias.put("loginDate", "最后登录时间");
        export(list, headAlias);
    }

    /**
     * 导入用户信息
     *
     * @param file          导入文件
     * @param updateSupport 是否更新支持，如果已存在，则进行更新数据
     * @return 结果
     * @throws Exception 异常
     */
    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPerm('system:user:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        // 中文字段导入
        List<Map<String, Object>> readList = ExcelUtil.getReader(file.getInputStream()).readAll();
        List<SysUser> userList = new ArrayList<>();
        for (Map<String, Object> line : readList) {
            JSONObject json = JSONUtil.parseObj(line);
            SysUser sysUser = new SysUser();
            sysUser.setUserName(json.getStr("用户账号"));
            sysUser.setNickName(json.getStr("用户昵称"));
            sysUser.setEmail(json.getStr("用户邮箱"));
            sysUser.setPhoneNumber(json.getStr("手机号码"));
            sysUser.setSex(json.getStr("用户性别"));
            userList.add(sysUser);
        }
        String message = userService.importUser(userList, updateSupport, getUsername());
        return AjaxResult.success(message);
    }

    /**
     * 下载导入模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate() {
        Map<String, String> headAlias = new LinkedHashMap<>(8);
        headAlias.put("userName", "用户账号");
        headAlias.put("nickName", "用户昵称");
        headAlias.put("email", "用户邮箱");
        headAlias.put("phoneNumber", "手机号码");
        headAlias.put("sex", "用户性别");
        export(null, headAlias);
    }

    /**
     * 根据用户id获取详细信息
     *
     * @param userId 用户id
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:user:query')")
    @GetMapping(value = {"/", "/{userId}"})
    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        userService.checkUserDataScope(userId);
        AjaxResult ajax = AjaxResult.success();
        List<SysRole> roles = roleService.selectRoleAll();
        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        ajax.put("posts", postService.list());
        if (ObjectUtil.isNotNull(userId)) {
            SysUser sysUser = userService.selectUserById(userId);
            ajax.put(AjaxResult.DATA_TAG, userService.selectUserById(userId));
            ajax.put("postIds", postService.selectPostListByUserId(userId));
            ajax.put("roleIds", sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));
        }
        return ajax;
    }

    /**
     * 新增用户
     *
     * @param user 用户信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysUser user) {
        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(user))) {
            return AjaxResult.error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StrUtil.isNotEmpty(user.getPhoneNumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            return AjaxResult.error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StrUtil.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            return AjaxResult.error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     *
     * @param user 用户信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(user))) {
            return AjaxResult.error("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StrUtil.isNotEmpty(user.getPhoneNumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            return AjaxResult.error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StrUtil.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            return AjaxResult.error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户
     *
     * @param userIds 用户id数组
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds) {
        if (ArrayUtil.contains(userIds, getUserId())) {
            return error("当前用户不能删除");
        }
        return toAjax(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     *
     * @param user 用户信息（新密码）
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:user:resetPwd')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return toAjax(userService.resetPwd(user));
    }

    /**
     * 状态修改
     *
     * @param user 用户信息(状态)
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        return toAjax(userService.updateUserStatus(user));
    }

    /**
     * 根据用户编号获取授权角色
     *
     * @param userId 用户id
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:user:query')")
    @GetMapping("/authRole/{userId}")
    public AjaxResult authRole(@PathVariable("userId") Long userId) {
        AjaxResult ajax = AjaxResult.success();
        SysUser user = userService.selectUserById(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        ajax.put("user", user);
        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return ajax;
    }

    /**
     * 用户授权角色
     *
     * @param userId  用户id
     * @param roleIds 角色id集合
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PutMapping("/authRole")
    public AjaxResult insertAuthRole(Long userId, Long[] roleIds) {
        userService.checkUserDataScope(userId);
        userService.insertUserAuth(userId, roleIds);
        return success();
    }

    /**
     * 获取部门树列表
     *
     * @param dept 查询条件
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:user:list')")
    @GetMapping("/deptTree")
    public AjaxResult deptTree(SysDept dept) {
        return AjaxResult.success(deptService.selectDeptTreeList(dept));
    }
}
