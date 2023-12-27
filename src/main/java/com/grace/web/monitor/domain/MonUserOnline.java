package com.grace.web.monitor.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 在线用户
 *
 * @author chanfa
 */
@Data
public class MonUserOnline implements Serializable {

    @Serial
    private static final long serialVersionUID = 1520692505403552887L;

    /**
     * 会话ID
     */
    private String tokenId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地址
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
     * 登录时间
     */
    private Long loginTime;
}
