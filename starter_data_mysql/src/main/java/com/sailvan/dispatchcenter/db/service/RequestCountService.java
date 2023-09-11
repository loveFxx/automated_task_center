package com.sailvan.dispatchcenter.db.service;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.common.domain.BusinessSystem;
import com.sailvan.dispatchcenter.common.domain.RequestCount;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.common.util.WebTokenUtil;
import com.sailvan.dispatchcenter.db.dao.automated.RequestCountDao;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author menghui
 * @date 21-08
 */
@Service
public class RequestCountService implements com.sailvan.dispatchcenter.common.pipe.RequestCountService {

    private static Logger logger = LoggerFactory.getLogger(RequestCountService.class);

    @Autowired
    private RequestCountDao requestCountDao;

    @Autowired
    RedisUtils redisUtils;


    @Override
    public List<RequestCount> getRequestCountAll() {
        return requestCountDao.getRequestCountAll();
    }

    @Override
    public List<RequestCount> getRequestCountByPeriod(String period) {
        return requestCountDao.getRequestCountByPeriod(period);
    }

    @Override
    public int updateRequestCount(RequestCount requestCount) {
        return requestCountDao.updateRequestCount(requestCount);
    }

    @Override
    public int insertRequestCount(RequestCount requestCount) {
        return requestCountDao.insertRequestCount(requestCount);
    }

    @Override
    public List<RequestCount> selectByPeriodAndSystemName(RequestCount requestCount) {
        return requestCountDao.selectByPeriodAndSystemName(requestCount);
    }
}
