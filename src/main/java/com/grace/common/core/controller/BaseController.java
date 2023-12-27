package com.grace.common.core.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.grace.common.config.CommonConfig;
import com.grace.common.core.domain.AjaxResult;
import com.grace.common.core.domain.LoginUser;
import com.grace.common.exception.CustomException;
import com.grace.common.utils.SecurityUtils;
import com.grace.common.utils.ServletUtils;
import org.apache.poi.ss.util.SheetUtil;

import java.util.Collection;
import java.util.Map;

/**
 * web层通用数据处理
 *
 * @author chanfa
 */
public class BaseController {

    /**
     * 当前记录起始索引
     */
    public static final String PAGE_NUM = "pageNum";

    /**
     * 每页显示记录数
     */
    public static final String PAGE_SIZE = "pageSize";

    /**
     * 排序列
     */
    public static final String ORDER_BY = "orderByColumn";

    /**
     * 排序列
     */
    public static final String IS_ASC = "isAsc";

    /**
     * 排序类型：ASC
     */
    public static final String ASC = "ascending";

    /**
     * 排序类型：DESC
     */
    public static final String DESC = "descending";


    /**
     * 返回成功
     */
    public AjaxResult success() {
        return AjaxResult.success();
    }

    /**
     * 返回失败消息
     */
    public AjaxResult error() {
        return AjaxResult.error();
    }

    /**
     * 返回失败消息
     */
    public AjaxResult error(String message) {
        return AjaxResult.error(message);
    }

    /**
     * 响应返回结果
     *
     * @param rows 影响行数
     * @return 操作结果
     */
    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 响应返回结果
     *
     * @param result 结果
     * @return 操作结果
     */
    protected AjaxResult toAjax(boolean result) {
        return result ? success() : error();
    }

    /**
     * 获取用户缓存信息
     */
    public LoginUser getLoginUser() {
        return SecurityUtils.getLoginUser();
    }

    /**
     * 获取登录用户id
     */
    public Long getUserId() {
        return getLoginUser().getUserId();
    }

    /**
     * 获取登录用户名
     */
    public String getUsername() {
        return getLoginUser().getUsername();
    }

    /**
     * 导出
     *
     * @param list      导出结果集，下载模板时传null
     * @param headAlias 导出表头
     */
    protected void export(Collection<?> list, Map<String, String> headAlias) {
        try {
            String filename = IdUtil.getSnowflake().nextId() + ".xlsx";
            String filePath = CommonConfig.getDownloadPath() + filename;
            ExcelWriter writer = ExcelUtil.getWriter(filePath);
            if (CollUtil.isEmpty(list)) {
                // 下载模板：只写标题行
                writer.writeHeadRow(headAlias.values());
            } else {
                // 导出数据：写入标题行和数据行
                writer.setHeaderAlias(headAlias);
                writer.setOnlyAlias(true);
                writer.write(list, true);
            }
            //自适应宽度
            int columnCount = writer.getColumnCount();
            for (int i = 0; i < columnCount; ++i) {
                double width = SheetUtil.getColumnWidth(writer.getSheet(), i, false);
                if (width != -1) {
                    if (width > 100) {
                        width = 100;
                        writer.setColumnWidth(i, Math.toIntExact(Math.round(width)));
                    } else {
                        width *= 256;
                        //此处可以适当调整，调整列空白处宽度
                        width += 4000;
                        writer.setColumnWidth(i, Math.toIntExact(Math.round(width / 256)));
                    }
                }
            }
            // 返回输出流
            writer.flush(ServletUtils.getResponse().getOutputStream(), true);
            writer.close();
            // 导出后删除文件
            FileUtil.del(filePath);
        } catch (Exception e) {
            throw new CustomException("导出失败");
        }
    }
}
