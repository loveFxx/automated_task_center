package com.sailvan.dispatchcenter.common.remote;

import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.config.CoreServiceAddressConfig;

import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.domain.TaskSourceList;
import com.sailvan.dispatchcenter.common.util.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * 远程调用core
 *
 * @author menghui
 * @date 2021-10
 */
@Component
public class RemotePushTaskUpdate {

    private static Logger logger = LoggerFactory.getLogger(RemotePushTaskUpdate.class);

    @Autowired
    HttpClientUtils httpClient;

    @Autowired
    CoreServiceAddressConfig addressConfig;

    public void remoteUpdate(Task task) {
        String url = "http://" + addressConfig.getIp() + ":" + addressConfig.getPort() + "/remote/update";
        try {
            httpClient.post(url, buildHttpEntity(task));
        } catch (Exception e) {
            logger.error("remoteUpdate url:{}, task:{} ,taskName:{}, message:{}", url, task, task.getTaskName(), e.getMessage());
        }
    }

    public void remoteUpdateConcurrency(Task task) {
        String url = "http://" + addressConfig.getIp() + ":" + addressConfig.getPort() + "/remote/update_concurrency";
        try {
            httpClient.post(url, buildHttpEntity(task));
        } catch (Exception e) {
            logger.error("remoteUpdate url:{}, task:{} ,taskName:{}, message:{}", url, task, task.getTaskName(), e.getMessage());
        }
    }

    public void remotePause(Task task) {
        String url = "http://" + addressConfig.getIp() + ":" + addressConfig.getPort() + "/remote/pause";
        try {
            httpClient.post(url, buildHttpEntity(task));
        } catch (Exception e) {
            logger.error("remotePause url:{}, task:{} ,taskName:{}, message:{}", url, task, task.getTaskName(), e.getMessage());
        }
    }

    public void remoteResume(Task task) {
        String url = "http://" + addressConfig.getIp() + ":" + addressConfig.getPort() + "/remote/resume";
        try {
            httpClient.post(url, buildHttpEntity(task));
        } catch (Exception e) {
            logger.error("remoteResume url:{}, task:{} ,taskName:{}, message:{}", url, task, task.getTaskName(), e.getMessage());
        }
    }

    public void remoteRePushCircleTasks(JSONObject json) {
        String url = "http://" + addressConfig.getIp() + ":" + addressConfig.getPort() + "/remote/repush_circle_tasks";
        try {
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(json);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json; charset=UTF-8");
            HttpEntity<String> httpEntity = new HttpEntity<>(jsonObject.toJSONString(), headers);
            httpClient.post(url, httpEntity);
        } catch (Exception e) {
            logger.error("remoteRepushCircleTasks url:{}, json:{}, message:{}", url,json, e.getMessage());
        }
    }


    private HttpEntity<String> buildHttpEntity(Task task) {
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(task);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonObject.toJSONString(), headers);
        return httpEntity;
    }

    public void remoteDelete(String id) {
        String url = "http://" + addressConfig.getIp() + ":" + addressConfig.getPort() + "/remote/delete?id="+id;
        try {
            httpClient.post(url, buildHttpEntity(new Task()));
        } catch (Exception e) {
            logger.error("remoteDelete url:{}, id:{} , message:{}", url, id, e.getMessage());
        }
    }

    private HttpEntity<String> buildHttpEntityTaskSourceList(TaskSourceList taskSourceList) {
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(taskSourceList);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonObject.toJSONString(), headers);
        return httpEntity;
    }

    public void remotePush(TaskSourceList taskSourceList) {
        String url = "http://" + addressConfig.getIp() + ":" + addressConfig.getPort() + "/remote/repush";
        try {
            httpClient.post(url, buildHttpEntityTaskSourceList(taskSourceList));
        } catch (Exception e) {
            logger.error("remoteUpdate url:{}, task:{} ,taskName:{}, message:{}", url, e.getMessage());
        }
    }

    public String getDelayQueue() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        String post = "";
        String url = "http://" + addressConfig.getIp() + ":" + addressConfig.getPort() + "/remote/getDelayQueue";
        try {
             post = httpClient.post(url, httpEntity);
            //System.out.println(post);
        } catch (Exception e) {
            logger.error("remoteUpdate url:{}, task:{} ,taskName:{}, message:{}", url, e.getMessage());
        }
        return post;
    }
}
