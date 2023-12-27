package com.grace.web.monitor.domain;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志记录表
 *
 * @author chanfa
 */
@Data
@TableName("mon_oplog")
public class MonOplog implements Serializable {

    @Serial
    private static final long serialVersionUID = 7303420550813997408L;

    /**
     * 操作日志ID
     */
    @TableId
    private Long oplogId;

    /**
     * 操作模块
     */
    @TableField(condition = SqlCondition.LIKE)
    private String title;

    /**
     * 业务类型（0其它 1新增 2修改 3删除）
     */
    private Integer businessType;

    /**
     * 业务类型数组
     */
    @TableField(exist = false)
    private Integer[] businessTypes;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 操作类别（0其它 1后台用户 2手机端用户）
     */
    private Integer operatorType;

    /**
     * 操作人员
     */
    @TableField(condition = SqlCondition.LIKE)
    private String oplogName;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 请求url
     */
    private String oplogUrl;

    /**
     * 操作地址
     */
    private String oplogIp;

    /**
     * 操作地点
     */
    private String oplogLocation;

    /**
     * 请求参数
     */
    private String oplogParam;

    /**
     * 返回参数
     */
    private String jsonResult;

    /**
     * 操作状态（0正常 1异常）
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    private LocalDateTime oplogTime;

    /**
     * 请求参数
     */
    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>(16);
}
