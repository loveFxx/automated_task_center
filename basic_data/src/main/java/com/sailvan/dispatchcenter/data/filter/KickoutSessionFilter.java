package com.sailvan.dispatchcenter.data.filter;

import com.sailvan.dispatchcenter.common.domain.system.UserDomain;
import com.sailvan.dispatchcenter.common.response.ResponseResult;
import com.sailvan.dispatchcenter.common.util.IStatusMessage;
import com.sailvan.dispatchcenter.common.util.ShiroFilterUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;


/**
 * @Title: KickouSessionFilter
 * @Description: 进行用户访问控制
 * @date 2021-04
 * @author menghui
 *
 */
public class KickoutSessionFilter extends AccessControlFilter {

    private static final Logger logger = LoggerFactory.getLogger(KickoutSessionFilter.class);

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 踢出后到的地址
     */
    private String kickOutUrl;

    /**
     * 踢出之前登录的/之后登录的用户 默认false踢出之前登录的用户
     */
    private boolean kickOutAfter = false;

    /**
     *  同一个帐号最大会话数 默认1
     */
    private int maxSession = 1;
    private SessionManager sessionManager;
    private Cache<String, Deque<Serializable>> cache;

    public void setKickOutUrl(String kickOutUrl) {
        this.kickOutUrl = kickOutUrl;
    }

    public void setKickOutAfter(boolean kickOutAfter) {
        this.kickOutAfter = kickOutAfter;
    }

    public void setMaxSession(int maxSession) {
        this.maxSession = maxSession;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * 设置Cache的key的前缀
     * @param cacheManager
     */
    public void setCacheManager(CacheManager cacheManager) {
        //必须和ehcache缓存配置中的缓存name一致
        this.cache = cacheManager.getCache("shiro-activeSessionCache");
    }



    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response)throws Exception {
        Subject subject = getSubject(request,response);
        // 没有登录授权 且没有记住我
        if(!subject.isAuthenticated() && !subject.isRemembered()){
            // 如果没有登录，直接进行之后的流程
            return true;
        }
        // 获得用户请求的URI
        HttpServletRequest req=(HttpServletRequest) request;
        String path = req.getRequestURI();

        String login = "/login";
        if(login.equals(path)){
            return true;
        }
        Session session = subject.getSession();
//        logger.info("session时间设置：" + String.valueOf(session.getTimeout()));


        try{
            // 当前用户
            UserDomain user = (UserDomain) subject.getPrincipal();
            String username = user.getSysUserName();
//            logger.info("===当前用户username:" + username);
            Serializable sessionId = session.getId();
            // 读取缓存用户 没有就存入
            Deque<Serializable> deque = cache.get(username);
            if (deque == null) {
                // 初始化队列
                deque = new ArrayDeque<Serializable>();
            }
            // 如果队列里没有此sessionId，且用户没有被踢出；放入队列
            String kickOut = "kickout";
            if (!deque.contains(sessionId) && session.getAttribute(kickOut) == null) {
                // 将sessionId存入队列
                deque.push(sessionId);
                // 将用户的sessionId队列缓存
                cache.put(username, deque);
            }
            // 如果队列里的sessionId数超出最大会话数，开始踢人
            while (deque.size() > maxSession) {
                logger.debug("===deque队列长度:" + deque.size());
                Serializable kickoutSessionId = null;
                /**
                 * 是否踢出后来登录的，默认是false；即后者登录的用户踢出前者登录的用户
                 *  如果踢出后者
                 */
                if (kickOutAfter) {
                    kickoutSessionId = deque.removeFirst();
                } else { // 否则踢出前者
                    kickoutSessionId = deque.removeLast();
                }
                // 踢出后再更新下缓存队列
                cache.put(username, deque);
                try{
                    // 获取被踢出的sessionId的session对象
                    Session kickoutSession = sessionManager
                            .getSession(new DefaultSessionKey(kickoutSessionId));
                    if (kickoutSession != null) {
                        // 设置会话的kickout属性表示踢出了
                        kickoutSession.setAttribute("kickout", true);
                    }
                }catch (Exception e){

                }
            }

            // 如果被踢出了，(前者或后者)直接退出，重定向到踢出后的地址
            if ((Boolean) session.getAttribute(kickOut) != null
                    && (Boolean) session.getAttribute("kickout") == true){
                // 会话被踢出了
                try {
                    // 退出登录
                    subject.logout();
                } catch (Exception e) { // ignore
                }
                saveRequest(request);
                logger.debug("===踢出后用户重定向的路径 kickOutUrl:{}" , kickOutUrl);
                return isAjaxResponse(request,response);
            }
            return true;
        }catch (Exception e){
            logger.error("控制用户在线数量【KickoutSessionFilter.onAccessDenied】异常！", e);
            return isAjaxResponse(request,response);
        }
    }


    public static void out(ServletResponse response, ResponseResult result){
        PrintWriter out = null;
        try {
            //设置编码
            response.setCharacterEncoding("UTF-8");
            //设置返回类型
            response.setContentType("application/json");
            out = response.getWriter();
            //输出
            out.println(OBJECT_MAPPER.writeValueAsString(result));
            logger.info("用户在线数量限制【KickoutSessionFilter.out】响应json信息成功");
        } catch (Exception e) {
            logger.error("用户在线数量限制【KickoutSessionFilter.out】响应json信息出错", e);
        }finally{
            if(null != out){
                out.flush();
                out.close();
            }
        }
    }

    private boolean isAjaxResponse(ServletRequest request,
                                   ServletResponse response) throws IOException {
        // ajax请求
        /**
         * 判断是否已经踢出
         * 1.如果是Ajax 访问，那么给予json返回值提示。
         * 2.如果是普通请求，直接跳转到登录页
         */
        //判断是不是Ajax请求
        ResponseResult responseResult = new ResponseResult();
        if (ShiroFilterUtils.isAjax(request) ) {
            logger.info(getClass().getName()+ "当前用户已经在其他地方登录，并且是Ajax请求！");
            responseResult.setCode(IStatusMessage.SystemStatus.MANY_LOGINS.getCode());
            responseResult.setMessage("您已在别处登录，请您修改密码或重新登录");
            out(response, responseResult);
        }else{
            // 重定向
            WebUtils.issueRedirect(request, response, kickOutUrl);
        }
        return false;
    }


}
