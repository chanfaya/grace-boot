package com.grace.web.monitor.domain;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.system.oshi.CpuInfo;
import cn.hutool.system.oshi.OshiUtil;
import com.grace.web.monitor.domain.server.*;
import lombok.Getter;
import lombok.Setter;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSFileStore;

import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * 服务器相关信息
 *
 * @author chanfa
 */
@Getter
@Setter
public class Server {

    /**
     * CPU相关信息
     */
    private Cpu cpu;

    /**
     * 內存相关信息
     */
    private Mem mem;

    /**
     * JVM相关信息
     */
    private Jvm jvm;

    /**
     * 服务器相关信息
     */
    private Sys sys;

    /**
     * 磁盘相关信息
     */
    private List<SysFile> sysFiles;

    public Server() {
        cpu = new Cpu();
        mem = new Mem();
        jvm = new Jvm();
        sys = new Sys();
        sysFiles = new LinkedList<>();
    }

    public void copyTo() {
        setCpuInfo();
        setMemInfo();
        setSysInfo();
        setJvmInfo();
        setSysFiles();
    }

    /**
     * 设置CPU信息
     */
    private void setCpuInfo() {
        CpuInfo cpuInfo = OshiUtil.getCpuInfo();
        cpu.setCpuNum(cpuInfo.getCpuNum());
        cpu.setTotal(cpuInfo.getToTal());
        cpu.setSys(cpuInfo.getSys());
        cpu.setUsed(cpuInfo.getUsed());
        cpu.setWait(cpuInfo.getWait());
        cpu.setFree(cpuInfo.getFree());
    }

    /**
     * 设置服务器信息
     */
    private void setSysInfo() {
        Properties props = System.getProperties();
        sys.setComputerName(NetUtil.getLocalHostName());
        sys.setComputerIp(NetUtil.getLocalhostStr());
        sys.setOsName(props.getProperty("os.name"));
        sys.setOsArch(props.getProperty("os.arch"));
        sys.setUserDir(props.getProperty("user.dir"));
    }

    /**
     * 设置Java虚拟机
     */
    private void setJvmInfo() {
        Properties props = System.getProperties();
        long total = Runtime.getRuntime().totalMemory();
        long max = Runtime.getRuntime().maxMemory();
        long free = Runtime.getRuntime().freeMemory();
        long used = total - free;
        jvm.setTotal(NumberUtil.div(total, (1024 * 1024), 2));
        jvm.setMax(NumberUtil.div(max, (1024 * 1024), 2));
        jvm.setFree(NumberUtil.div(free, (1024 * 1024), 2));
        jvm.setUsed(NumberUtil.div(used, (1024 * 1024), 2));
        jvm.setUsage(NumberUtil.mul(NumberUtil.div(used, total, 4), 100));
        jvm.setVersion(props.getProperty("java.version"));
        jvm.setHome(props.getProperty("java.home"));
        jvm.setName(ManagementFactory.getRuntimeMXBean().getVmName());
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        jvm.setStartTime(DateUtil.formatDateTime(DateUtil.date(time)));
        jvm.setRunTime(DateUtil.formatBetween(DateUtil.date(), DateUtil.date(time)));
        jvm.setInputArgs(ManagementFactory.getRuntimeMXBean().getInputArguments().toString());
    }

    /**
     * 设置内存信息
     */
    private void setMemInfo() {
        GlobalMemory memory = OshiUtil.getMemory();
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;
        mem.setTotal(NumberUtil.div(total, (1024 * 1024 * 1024), 2));
        mem.setUsed(NumberUtil.div(used, (1024 * 1024 * 1024), 2));
        mem.setFree(NumberUtil.div(available, (1024 * 1024 * 1024), 2));
        mem.setUsage(NumberUtil.mul(NumberUtil.div(used, total, 4), 100));
    }

    /**
     * 设置磁盘信息
     */
    private void setSysFiles() {
        List<OSFileStore> fsArray = OshiUtil.getOs().getFileSystem().getFileStores();
        for (OSFileStore fs : fsArray) {
            long free = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            long used = total - free;
            SysFile sysFile = new SysFile();
            sysFile.setDirName(fs.getMount());
            sysFile.setSysTypeName(fs.getType());
            sysFile.setTypeName(fs.getName());
            sysFile.setTotal(convertFileSize(total));
            sysFile.setFree(convertFileSize(free));
            sysFile.setUsed(convertFileSize(used));
            if (total == 0L) {
                // total为0
                sysFile.setUsage(0);
            } else {
                sysFile.setUsage(NumberUtil.mul(NumberUtil.div(used, total, 4), 100));
            }
            sysFiles.add(sysFile);
        }
    }

    /**
     * 字节转换
     *
     * @param size 字节大小
     * @return 转换后值
     */
    public String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }
    }
}
