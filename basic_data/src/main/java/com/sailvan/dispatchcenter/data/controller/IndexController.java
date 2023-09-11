package com.sailvan.dispatchcenter.data.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 索引
 * @date 2021-04
 * @author menghui
 */
@Controller
public class IndexController {

    @RequestMapping({"/","login"})
    public String login(){
        return "login2";
    }

    @RequestMapping("/index")
    public String index(){
        return "index";
    }

    @RequestMapping("home")
    public String home(){
        return "home";
    }

    @RequestMapping("/permission/userManage")
    public String userManage(){
        return "permission/userManage";
    }

    @RequestMapping("/permission/roleManage")
    public String roleManage(){
        return "permission/roleManage";
    }

    @RequestMapping("/permission/permissionManage")
    public String permissionManage(){
        return "permission/permissionManage";
    }

    @RequestMapping("machine/machineManage")
    public String crawler(){
        return "machine/machineManage";
    }

    @RequestMapping("machine/lambdaUser")
    public String lambdaUser(){
        return "machine/lambdaUser";
    }

    @RequestMapping("monitor/machineMonitor")
    public String machineMonitor(){
        return "monitor/machineMonitor";
    }


    @RequestMapping("monitor/proxyIPMonitor")
    public String proxyIPMonitor(){
        return "monitor/proxyIPMonitor";
    }

    @RequestMapping("monitor/taskMonitor")
    public String taskMonitor(){
        return "monitor/taskMonitor";
    }

    @RequestMapping("monitor/taskFailMonitor")
    public String taskFailMonitor(){
        return "monitor/taskFailMonitor";
    }

    @RequestMapping("monitor/requestCountMonitor")
    public String requestCountMonitor(){
        return "monitor/requestCountMonitor";
    }

    @RequestMapping("monitor/accountMonitor")
    public String accountMonitor(){
        return "monitor/accountMonitor";
    }

    @RequestMapping("task/taskManage")
    public String taskManage(){
        return "task/taskManage";
    }

    @RequestMapping("task/taskSourceList")
    public String taskSourceList(){
        return "task/taskSourceList";
    }

    @RequestMapping("task/taskLogs")
    public String taskLogs(){
        return "task/taskLogs";
    }

    @RequestMapping("task/taskResult")
    public String taskResult(){
        return "task/taskResult";
    }

    @RequestMapping("store/storeAccountManage")
    public String storeAccountManage(){
        return "store/storeAccountManage";
    }

    @RequestMapping("proxyIP/proxyIPManage")
    public String proxyIpManage(){
        return "proxyIP/proxyIPManage";
    }

    @RequestMapping("businessS/businessSystem")
    public String businessSystem(){
        return "businessS/businessSystem";
    }

    @RequestMapping("version/versionManage")
    public String versionManage(){
        return "version/versionManage";
    }

    @RequestMapping("apiKey/apiKeyManage")
    public String apiKeyManage(){
        return "apiKey/apiKeyManage";
    }

    @RequestMapping("platform/platformManage")
    public String platformManage(){
        return "platform/platformManage";
    }

    @RequestMapping("signOut")
    public String signOut(HttpServletRequest request){
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            subject.logout();
        }
        return "redirect:login";
    }

    @RequestMapping("businessS/businessSystemTask")
    public String businessSystemTask(){
        return "businessS/businessSystemTask";
    }

}
