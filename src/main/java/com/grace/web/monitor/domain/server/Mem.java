package com.grace.web.monitor.domain.server;

import lombok.Getter;
import lombok.Setter;

/**
 * Mem
 *
 * @author chanfa
 */
@Getter
@Setter
public class Mem {

    /**
     * 内存总量
     */
    private double total;

    /**
     * 已用内存
     */
    private double used;

    /**
     * 剩余内存
     */
    private double free;

    /**
     * 使用率
     */
    private double usage;
}
