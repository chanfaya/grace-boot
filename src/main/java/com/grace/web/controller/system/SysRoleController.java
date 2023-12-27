package com.grace.web.controller.system;

import cn.hutool.core.util.ObjectUtil;
import com.grace.common.annotation.Log;
import com.grace.common.constant.UserConstants;
import com.grace.common.core.controller.BaseController;
import com.grace.common.core.domain.AjaxResult;
import com.grace.common.core.domain.LoginUser;
import com.grace.common.enums.BusinessType;
import com.grace.framework.web.service.SysPermissionService;
import com.grace.framework.web.service.TokenService;
import com.grace.web.system.domain.SysDept;
import com.grace.web.system.domain.SysRole;
import com.grace.web.system.domain.SysUser;
import com.grace.web.system.domain.SysUserRole;
import com.grace.web.system.service.ISysDeptService;
import com.grace.web.system.service.ISysRoleService;
import com.grace.web.system.service.ISysUserService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色管理
 *
 * @author chanfa
 */
@RestController
@RequestMapping("/system/role")
public class SysRoleController extends BaseController {

    @Resource
    private ISysRoleService roleService;

    @Resource
    private TokenService tokenService;

    @Resource
    private SysPermissionService permissionService;

    @Resource
    private ISysUserService userService;

    @Resource
    private ISysDeptService deptService;

    /**
     * 角色列表
     *
     * @param role 查询条件
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:role:list')")
    @GetMapping("/list")
    public AjaxResult list(SysRole role) {
        List<SysRole> list = roleService.selectRoleList(role);
        return AjaxResult.success(list);
    }

    /**
     * 获取对应角色部门树列表
     *
     * @param roleId 角色id
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:role:query')")
    @GetMapping(value = "/deptTree/{roleId}")
    public AjaxResult deptTree(@PathVariable("roleId") Long roleId) {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", deptService.selectDeptListByRoleId(roleId));
        ajax.put("deptList", deptService.selectDeptTreeList(new SysDept()));
        return ajax;
    }

    /**
     * 导出角色管理
     *
     * @param role 查询条件
     */
    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPerm('system:role:export')")
    @PostMapping("/export")
    public void export(SysRole role) {
        List<SysRole> list = roleService.selectRoleList(role);
        Map<String, String> headAlias = new LinkedHashMap<>(8);
        headAlias.put("roleId", "角色ID");
        headAlias.put("roleName", "角色名称");
        headAlias.put("roleKey", "角色权限");
        headAlias.put("roleSort", "角色排序");
        export(list, headAlias);
    }

    /**
     * 根据角色id获取详细信息
     *
     * @param roleId 角色id
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:role:query')")
    @GetMapping(value = "/{roleId}")
    public AjaxResult getInfo(@PathVariable Long roleId) {
        roleService.checkRoleDataScope(roleId);
        return AjaxResult.success(roleService.selectRoleById(roleId));
    }

    /**
     * 新增角色
     *
     * @param role 角色信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:role:add')")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysRole role) {
        if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleNameUnique(role))) {
            return AjaxResult.error("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleKeyUnique(role))) {
            return AjaxResult.error("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        return toAjax(roleService.insertRole(role));
    }

    /**
     * 修改保存角色
     *
     * @param role 角色信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleNameUnique(role))) {
            return AjaxResult.error("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleKeyUnique(role))) {
            return AjaxResult.error("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }

        if (roleService.updateRole(role) > 0) {
            // 更新缓存用户权限
            LoginUser loginUser = getLoginUser();
            if (ObjectUtil.isNotNull(loginUser.getUser()) && !loginUser.getUser().isAdmin()) {
                loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
                loginUser.setUser(userService.selectUserByUserName(loginUser.getUser().getUserName()));
                tokenService.setLoginUser(loginUser);
            }
            return AjaxResult.success();
        }
        return AjaxResult.error("修改角色'" + role.getRoleName() + "'失败，请联系管理员");
    }

    /**
     * 修改保存数据权限
     *
     * @param role 数据权限
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/dataScope")
    public AjaxResult dataScope(@RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        return toAjax(roleService.authDataScope(role));
    }

    /**
     * 状态修改
     *
     * @param role 角色信息（状态）
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        return toAjax(roleService.updateRoleStatus(role));
    }

    /**
     * 删除角色
     *
     * @param roleIds 角色id数组
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:role:remove')")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    public AjaxResult remove(@PathVariable Long[] roleIds) {
        return toAjax(roleService.deleteRoleByIds(roleIds));
    }

    /**
     * 获取角色选择框列表
     *
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:role:query')")
    @GetMapping("/optionSelect")
    public AjaxResult optionSelect() {
        return AjaxResult.success(roleService.selectRoleAll());
    }

    /**
     * 查询已分配用户角色列表
     *
     * @param user 用户信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:role:list')")
    @GetMapping("/authUser/allocatedList")
    public AjaxResult allocatedList(SysUser user) {
        List<SysUser> list = userService.selectAllocatedList(user);
        return AjaxResult.success(list);
    }

    /**
     * 查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:role:list')")
    @GetMapping("/authUser/unallocatedList")
    public AjaxResult unallocatedList(SysUser user) {
        List<SysUser> list = userService.selectUnallocatedList(user);
        return AjaxResult.success(list);
    }

    /**
     * 取消授权用户
     *
     * @param userRole 用户角色
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancel")
    public AjaxResult cancelAuthUser(@RequestBody SysUserRole userRole) {
        return toAjax(roleService.deleteAuthUser(userRole));
    }

    /**
     * 批量取消授权用户
     *
     * @param roleId  角色id
     * @param userIds 用户id集合
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancelAll")
    public AjaxResult cancelAuthUserAll(Long roleId, Long[] userIds) {
        return toAjax(roleService.deleteAuthUsers(roleId, userIds));
    }

    /**
     * 批量选择用户授权
     *
     * @param roleId  角色id
     * @param userIds 用户id集合
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/selectAll")
    public AjaxResult selectAuthUserAll(Long roleId, Long[] userIds) {
        roleService.checkRoleDataScope(roleId);
        return toAjax(roleService.insertAuthUsers(roleId, userIds));
    }
}
