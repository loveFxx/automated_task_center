package com.sailvan.dispatchcenter.data.controller.system;

import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.common.domain.LoginDTO;
import com.sailvan.dispatchcenter.common.domain.UserSearch;
import com.sailvan.dispatchcenter.common.domain.system.UserDomain;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.db.service.system.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 系统用户管理 UserController
 * @date 2021-04
 * @author menghui
 */
@Controller
@RequestMapping("user")
public class UserController {

    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;


    /**
     *
     * 功能描述: 登入系统
     *
     * @param:
     * @return:
     */
    @RequestMapping("login")
    @ResponseBody
    public ApiResponseDomain login(HttpServletRequest request, LoginDTO loginDTO, HttpSession session){
        if (logger.isTraceEnabled()) {
            logger.trace("进行登陆");
        }
        ApiResponse apiResponse = new ApiResponse();

        // 使用 shiro 进行登录
        Subject subject = SecurityUtils.getSubject();
        String userName = loginDTO.getUsername().trim();
        String password = loginDTO.getPassword().trim();
        String rememberMe = loginDTO.getRememberMe();
        String host = request.getRemoteAddr();

        //获取token
        UsernamePasswordToken token = new UsernamePasswordToken(userName, password,host);
        // 设置 remenmberMe 的功能
        String on = "on";
        if (rememberMe != null && on.equals(rememberMe)) {
            token.setRememberMe(true);
        }
        JSONObject content = new JSONObject();

        try {
            subject.login(token);
            // 登录成功
            UserDomain user = (UserDomain) subject.getPrincipal();
            session.setAttribute("user", user.getSysUserName());
            session.setAttribute("user", userName);

            content.put("url","/home");

            HttpSession httpSession = request.getSession();
            httpSession.setAttribute(Constant.SYSTEM_USER_SESSION,loginDTO);
            // 3小时 用户登录凭据失效
            httpSession.setMaxInactiveInterval(60*60*3);
            if (logger.isTraceEnabled()) {
                logger.trace(user.getSysUserName()+"登陆成功");
            }
        } catch (UnknownAccountException e) {
            logger.error(userName+"账号不存在");
            return apiResponse.error(ResponseCode.ERROR_CODE,userName+"账号不存在","");
        }catch (DisabledAccountException e){
            logger.error(userName+"账号异常");
            return apiResponse.error(ResponseCode.ERROR_CODE,userName+"账号异常","");
        } catch (AuthenticationException e){
            logger.error(userName+"密码错误");
            return apiResponse.error(ResponseCode.ERROR_CODE,userName+"密码错误","");
        }

        return apiResponse.success("成功",content);
    }

    /**
     *
     * 功能描述: 修改密码
     *
     */
    @RequestMapping("setPwd")
    @ResponseBody
    public ApiResponseDomain setP(String pwd, String isPwd){
        ApiResponse apiResponse = new ApiResponse();
        if (logger.isTraceEnabled()) {
            logger.trace("进行密码重置");
        }
        if(!pwd.equals(isPwd)){
            logger.error("两次输入的密码不一致!");
            return apiResponse.error(ResponseCode.ERROR_CODE,"两次输入的密码不一致!","");
        }
        //获取当前登陆的用户信息
        UserDomain user = (UserDomain) SecurityUtils.getSubject().getPrincipal();
        int result = userService.updatePwd(user.getSysUserName(),pwd);
        if(result == 0){
            logger.error("用户修改密码失败！");
            return apiResponse.error(ResponseCode.ERROR_CODE,"修改密码失败!","");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("用户修改密码成功！");
        }
        return apiResponse.success("修改密码成功!","");
    }

    /**
     *
     * 功能描述: 跳到系统用户列表
     *
     */
    @RequestMapping("/userManage")
    public String userManage() {
        return "/user/userManage";
    }

    /**
     *
     * 功能描述: 分页查询用户列表
     *
     */
    @RequestMapping(value = "/getUserList", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getUserList(@RequestParam("pageNum") Integer pageNum,
                                      @RequestParam("pageSize") Integer pageSize,/*@Valid PageRequest page,*/ UserSearch userSearch) {
        PageDataResult pdr = new PageDataResult();
        try {
            if(null == pageNum) {
                pageNum = 1;
            }
            if(null == pageSize) {
                pageSize = 10;
            }
            // 获取用户列表
            pdr = userService.getUserList(userSearch, pageNum ,pageSize);
            if (logger.isTraceEnabled()) {
                logger.trace("用户列表查询=pdr:" + pdr);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("用户列表查询异常！", e);
        }
        return pdr;
    }


    /**
     *
     * 功能描述: 新增和更新系统用户
     *
     */
    @RequestMapping(value = "/setUser", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain setUser(UserDomain user) {
        logger.info("设置用户[新增或更新]！user:" + user);
        if(user.getId() == null){
            return userService.addUser(user);
        }else{
            return userService.updateUser(user);
        }
    }


    /**
     *
     * 功能描述: 删除/恢复 用户
     *
     */
    @RequestMapping(value = "/updateUserStatus", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain updateUserStatus(@RequestParam("id") Integer id, @RequestParam("status") Integer status) {
        if (logger.isTraceEnabled()) {
            logger.trace("删除/恢复用户！id:" + id+" status:"+status);
        }
        if(status == 0){
            //删除用户
            return userService.delUser(id,status);
        }else{
            //恢复用户
            return userService.recoverUser(id,status);
        }
    }


}
