package com.grace.web.monitor.domain.server;

import lombok.Getter;
import lombok.Setter;

/**
 * Jvm
 *
 * @author chanfa
 */
@Getter
@Setter
public class Jvm {

    /**
     * 当前JVM占用的内存总数(M)
     */
    private double total;

    /**
     * JVM最大可用内存总数(M)
     */
    private double max;

    /**
     * JVM空闲内存(M)
     */
    private double free;

    /**
     * JVM已用内存(M)
     */
    private double used;

    /**
     * JVM内存使用率(M)
     */
    private double usage;

    /**
     * JDK版本
     */
    private String version;

    /**
     * JDK名称
     */
    private String name;

    /**
     * JDK路径
     */
    private String home;

    /**
     * JDK启动时间
     */
    private String startTime;

    /**
     * JDK运行时间
     */
    private String runTime;

    /**
     * 运行参数
     */
    private String inputArgs;
}
