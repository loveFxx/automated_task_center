package com.sailvan.dispatchcenter.common.constant;

/**
 * @program: automated_task_center
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-11-17 16:54
 **/
public class MonitorConstant {

    public static final String HEARTBEAT_STAT = "heartBeatStat";
    public static final String TASK_SUCCESS_STAT = "taskSuccessStat";
    public static final String TASK_FAILURE_REASON_STAT = "taskFailureReasonStat";
    public static final String CONTINENT_ACCOUNT_TASK_STAT = "continentAccountTaskStat";

    public static final String STAT_EXCEL_PATH = System.getProperty("user.dir") + "/stat.xlsx";
    public static final String MACHINE_STAT_SHEET_NAME = "machine stat sheet";
    public static final String TASK_STAT_SHEET_NAME = "task stat sheet";
    public static final String[] MACHINE_STAT_TITLE = new String[]{"机器id", "异常", "时间"};

    /**
     * cpu过高
     */
    public static final String MACHINE_CPU_THRESHOLD = "90";

    /**
     * 上次心跳距离现在的秒数
     */
    public static final int MACHINE_HEARTBEAT_THRESHOLD = 3600;

    /**
     * memory过高
     */
    public static final String MACHINE_MEMORY_THRESHOLD = "system_user_session";

    /**
     * 硬盘不足
     */
    public static final double MACHINE_DISKSPACE_THRESHOLD = 0.9;
}
