package com.sailvan.dispatchcenter.stat.monitor.controller;

import com.sailvan.dispatchcenter.stat.monitor.scheduler.TaskSuccessStatScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.ws.Action;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

@RestController
public class MonitorController {

    @Autowired
    TaskSuccessStatScheduler taskSuccessStatScheduler;

    @RequestMapping(value = "/send")
    public void machineExeTaskStat() {
        try {
            taskSuccessStatScheduler.sendTaskStatToWechatGroup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
