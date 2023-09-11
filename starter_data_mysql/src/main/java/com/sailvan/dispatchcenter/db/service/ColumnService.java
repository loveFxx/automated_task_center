package com.sailvan.dispatchcenter.db.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.domain.Column;
import com.sailvan.dispatchcenter.db.dao.automated.ColumnDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ColumnService implements com.sailvan.dispatchcenter.common.pipe.ColumnService {

    private static Logger logger = LoggerFactory.getLogger(ColumnService.class);

    @Resource
    private ColumnDao columnDao;

    /**
     * 获取拆分字段
     * @param taskId
     * @param isRequired
     * @return
     */
    @Override
    public List<Column> listColumnsByTaskIdAndIsRequired(int taskId, int isRequired){
        return columnDao.listColumnsByTaskIdAndIsRequired(taskId,isRequired);
    }

    @Override
    public List<Column> listColumnsByTaskId(int taskId){
        return columnDao.listColumnsByTaskId(taskId);
    }

    /**
     * 验证传递参数类型
     * @param type 参数类型
     * @param value 参数值
     * @return 参数类型是否匹配
     */
    @Override
    public boolean validateColumns(String type, Object value){
        boolean result = false;
        switch (type){
            case "string":
                if (value instanceof String){
                    result = true;
                }
                break;
            case "int":
                if (value instanceof Integer){
                    result = true;
                }
                break;
            case "float":
                if (value instanceof Float){
                    result = true;
                }
                break;
            case "datetime":
                if (value instanceof String){
                    try {
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date d1 = df.parse(String.valueOf(value));
                        result = true;
                    }catch (Exception e){
                        result = false;
                    }
                }
                break;
            case "date":
                if (value instanceof String){
                    try {
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        Date d1 = df.parse(String.valueOf(value));
                        result = true;
                    }catch (Exception e){
                        result = false;
                    }
                }
                break;
            case "json":
                if (value instanceof JSONArray || value instanceof JSONObject){
                    result = true;
                }
                break;
            default: break;
        }
        return result;
    }

}
