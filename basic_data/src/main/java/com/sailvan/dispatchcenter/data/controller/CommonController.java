package com.sailvan.dispatchcenter.data.controller;

import com.sailvan.dispatchcenter.common.domain.ProxyIp;
import com.sailvan.dispatchcenter.common.pipe.ProxyIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.sailvan.dispatchcenter.common.constant.Constant.COMMON_PREFIX;

@RestController
@RequestMapping(COMMON_PREFIX)
public class CommonController {

    @Autowired
    ProxyIpService proxyIpService;

    /**
     * 亚马逊进程代理IP搜索框
     * @return
     */
    @RequestMapping(value = "/amazonDaemonProxySearcher", method = RequestMethod.GET)
    @ResponseBody
    public Object amazonDaemonProxySearcher() {
        HashMap<String,String> searcher = new HashMap<>();
        List<ProxyIp> proxyIps = proxyIpService.getProxyByPlatform("7");
        for (ProxyIp proxyIp : proxyIps) {

            String ip;
            String[] split = String.valueOf(proxyIp.getIp()).split(":");
            if (split.length == 1){
                ip = split[0];
            }else {
                String[] split1 = split[1].split("-");
                ip = split1[6];
            }
            searcher.put(String.valueOf(proxyIp.getId()),ip);
        }
        return searcher;
    }
}
