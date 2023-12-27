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
 * 登录日志表
 *
 * @author chanfa
 */
@Data
@TableName("mon_syslog")
public class MonSyslog implements Serializable {

    @Serial
    private static final long serialVersionUID = -5095845395855884541L;

    /**
     * 登录日志ID
     */
    @TableId
    private Long syslogId;

    /**
     * 用户账号
     */
    @TableField(condition = SqlCondition.LIKE)
    private String userName;

    /**
     * 登录状态 0成功 1失败
     */
    private String status;

    /**
     * 登录IP地址
     */
    @TableField(condition = SqlCondition.LIKE)
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 提示消息
     */
    private String msg;

    /**
     * 访问时间
     */
    private LocalDateTime loginTime;

    /**
     * 请求参数
     */
    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>(16);
}
