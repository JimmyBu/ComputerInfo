package org.Computer.SpringBoot.Controller;

import org.hyperic.sigar.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;

@Controller
public class ComputerController {

    @RequestMapping("/list")
    public String list() {
        return "provider/list";
    }

    @RequestMapping("/CPU")
    private static String cpU(Map<String, Object> map, Model model) throws SigarException {
        Sigar sigar = new Sigar();
        CpuInfo infos[] = sigar.getCpuInfoList();
        CpuPerc cpuList[] = null;
        cpuList = sigar.getCpuPercList();
        List<Map<String, Object>> CPUList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < infos.length; i++) {    // 不管是单块CPU还是多CPU都适用
            CpuInfo info = infos[i];
            Map<String, Object> cpu = new HashMap<String, Object>();
            cpu.put("cpu", "第" + (i + 1) + "块CPU信息");
            cpu.put("cpu2", info.getMhz());
            cpu.put("cpu3", info.getVendor());
            cpu.put("cpu4", info.getModel());
            cpu.put("cpu5", info.getCacheSize());
            cpu.put("cpu6", cpuList[i]);
            CPUList.add(cpu);
            model.addAttribute("CPUList", CPUList);
            out.println("第" + (i + 1) + "块CPU信息");
            out.println("CPU的总量MHz:    " + info.getMhz());// CPU的总量MHz
            out.println("CPU生产商:    " + info.getVendor());// 获得CPU的生产商
            out.println("CPU类别:    " + info.getModel());// 获得CPU的类别
            out.println("CPU缓存数量:    " + info.getCacheSize());// 缓冲存储器数量
            printCpuPerc(cpuList[i]);
        }
        model.addAttribute("CPUList", CPUList);
        return "main/Cpu";
    }

    private static void printCpuPerc(CpuPerc cpu) {
        out.println("CPU用户使用率:    " + CpuPerc.format(cpu.getUser()));// 用户使用率
        out.println("CPU系统使用率:    " + CpuPerc.format(cpu.getSys()));// 系统使用率
        out.println("CPU当前等待率:    " + CpuPerc.format(cpu.getWait()));// 当前等待率
        out.println("CPU当前错误率:    " + CpuPerc.format(cpu.getNice()));// 当前错误率
        out.println("CPU当前空闲率:    " + CpuPerc.format(cpu.getIdle()));// 当前空闲率
        out.println("CPU总的使用率:    " + CpuPerc.format(cpu.getCombined()));// 总的使用率
    }

    @RequestMapping("/RAM")
    private static String memory(Map<String, Object> map) throws SigarException {
        Sigar sigar = new Sigar();
        Mem mem = sigar.getMem();
        // 内存总量
        System.out.println("内存总量:    " + mem.getTotal() / 1024L + "K av");
        // 当前内存使用量
        System.out.println("当前内存使用量:    " + mem.getUsed() / 1024L + "K used");
        // 当前内存剩余量
        System.out.println("当前内存剩余量:    " + mem.getFree() / 1024L + "K free");
        Swap swap = sigar.getSwap();
        // 交换区总量
        System.out.println("交换区总量:    " + swap.getTotal() / 1024L + "K av");
        // 当前交换区使用量
        System.out.println("当前交换区使用量:    " + swap.getUsed() / 1024L + "K used");
        // 当前交换区剩余量
        System.out.println("当前交换区剩余量:    " + swap.getFree() / 1024L + "K free");

        map.put("ram", mem.getTotal() / 1024L + "K InTotal\n");
        map.put("ram2", mem.getUsed() / 1024L + "K used\n");
        map.put("ram3", mem.getFree() / 1024L + "K free\n");
        map.put("ram4", swap.getTotal() / 1024L + "K InTotal\n");
        map.put("ram5", swap.getUsed() / 1024L + "K used\n");
        map.put("ram6", swap.getFree() / 1024L + "K free\n");
        map.put("ram7", DECIMALFORMAT.format(((double)mem.getUsed()/(double)mem.getTotal())*100)+"%");
        map.put("ram8", DECIMALFORMAT.format(((double)mem.getFree()/(double)mem.getTotal())*100)+"%");
        return "main/Ram";
    }
    private static DecimalFormat DECIMALFORMAT = new DecimalFormat("#.##");

    @RequestMapping("/ioUsage")
    public String getHdIOpPercent(Map<String, Object>map) throws SigarException{
        Sigar sigar = new Sigar();
        CpuInfo infos[] = sigar.getCpuInfoList();
        CpuPerc cpuList[] = null;
        cpuList = sigar.getCpuPercList();
        CpuPerc cpuPerc = sigar.getCpuPerc();
        System.out.println("开始收集磁盘IO使用率");
        double ioUsage = cpuPerc.getWait();
        Process pro = null;
        Runtime r = Runtime.getRuntime();
        try {
            String command = "iostat -d -x";
            pro = r.exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line = null;
            int count = 0;
            while ((line = in.readLine()) != null) {
                if (++count >= 4) {
//                  System.out.println(line);
                    String[] temp = line.split("\\s+");
                    if (temp.length > 1) {
                        float util = Float.parseFloat(temp[temp.length - 1]);
                        ioUsage = (ioUsage > util) ? ioUsage : util;
                    }
                }
            }
            if (ioUsage > 0) {
                System.out.println("本节点磁盘IO使用率为: " + ioUsage);
                ioUsage /= 100;
            }
            in.close();
            pro.destroy();
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            System.out.println("IoUsage发生InstantiationException. " + e.getMessage());
            System.out.println(sw.toString());
        }
        map.put("IO",ioUsage);
        return "main/ioUsage";
    }

}
