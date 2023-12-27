package com.grace.web.controller.system;

import cn.hutool.http.HttpUtil;
import com.grace.common.annotation.Log;
import com.grace.common.constant.UserConstants;
import com.grace.common.core.controller.BaseController;
import com.grace.common.core.domain.AjaxResult;
import com.grace.common.enums.BusinessType;
import com.grace.web.system.domain.SysMenu;
import com.grace.web.system.service.ISysMenuService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理
 *
 * @author chanfa
 */
@RestController
@RequestMapping("/system/menu")
public class SysMenuController extends BaseController {

    @Resource
    private ISysMenuService menuService;

    /**
     * 获取菜单列表
     *
     * @param menu 查询条件
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:menu:list')")
    @GetMapping("/list")
    public AjaxResult list(SysMenu menu) {
        List<SysMenu> menus = menuService.selectMenuList(menu, getUserId());
        return AjaxResult.success(menus);
    }

    /**
     * 根据菜单id获取详细信息
     *
     * @param menuId 菜单id
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:menu:query')")
    @GetMapping(value = "/{menuId}")
    public AjaxResult getInfo(@PathVariable Long menuId) {
        return AjaxResult.success(menuService.getById(menuId));
    }

    /**
     * 获取菜单下拉树列表
     *
     * @param menu 查询条件
     * @return 结果
     */
    @GetMapping("/treeSelect")
    public AjaxResult treeSelect(SysMenu menu) {
        List<SysMenu> menus = menuService.selectMenuList(menu, getUserId());
        return AjaxResult.success(menuService.buildMenuTreeSelect(menus));
    }

    /**
     * 加载对应角色菜单列表树
     *
     * @param roleId 角色id
     * @return 结果
     */
    @GetMapping(value = "/roleMenuTreeSelect/{roleId}")
    public AjaxResult roleMenuTreeSelect(@PathVariable("roleId") Long roleId) {
        List<SysMenu> menuList = menuService.selectMenuList(getUserId());
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", menuService.selectMenuListByRoleId(roleId));
        ajax.put("menuList", menuService.buildMenuTreeSelect(menuList));
        return ajax;
    }

    /**
     * 新增菜单
     *
     * @param menu 菜单信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:menu:add')")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysMenu menu) {
        if (UserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
            return AjaxResult.error("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame())
                && !HttpUtil.isHttp(menu.getPath()) && !HttpUtil.isHttps(menu.getPath())) {
            return AjaxResult.error("新增菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        }
        return toAjax(menuService.save(menu));
    }

    /**
     * 修改菜单
     *
     * @param menu 菜单信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:menu:edit')")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysMenu menu) {
        if (UserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
            return AjaxResult.error("修改菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame())
                && !HttpUtil.isHttp(menu.getPath()) && !HttpUtil.isHttps(menu.getPath())) {
            return AjaxResult.error("修改菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        } else if (menu.getMenuId().equals(menu.getParentId())) {
            return AjaxResult.error("修改菜单'" + menu.getMenuName() + "'失败，上级菜单不能选择自己");
        }
        return toAjax(menuService.updateById(menu));
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单id
     * @return 结果
     */
    @PreAuthorize("@ss.hasPerm('system:menu:remove')")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public AjaxResult remove(@PathVariable("menuId") Long menuId) {
        if (menuService.hasChildByMenuId(menuId)) {
            return AjaxResult.error("存在子菜单,不允许删除");
        }
        if (menuService.checkMenuExistRole(menuId)) {
            return AjaxResult.error("菜单已分配,不允许删除");
        }
        return toAjax(menuService.removeById(menuId));
    }
}
