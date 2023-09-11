package com.sailvan.dispatchcenter.data.controller;

import com.sailvan.dispatchcenter.db.config.TokenConfig;
import com.sailvan.dispatchcenter.db.service.TokenService;
import com.github.pagehelper.util.StringUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class TokenController {

    @Resource
    TokenService tokenService;

    @Resource
    TokenConfig tokenConfig;

    @RequestMapping("/token/callback")
    public String callback(HttpServletRequest request){
        String code = request.getParameter("code");
        String data = null;
        data = tokenService.requestToken(code);
        if (StringUtil.isNotEmpty(data)){
            tokenService.setCache(data);
        }
        return "success";
    }
    @RequestMapping("/token/redirect")
    public String redirect(){
        return "redirect:" + tokenConfig.getTokenUrl() + "/oauth/authorize?client_id=" + tokenConfig.getClientId()+"&redirect_uri=" + tokenConfig.getCallbackUrl() + "&response_type=code";
    }
}
