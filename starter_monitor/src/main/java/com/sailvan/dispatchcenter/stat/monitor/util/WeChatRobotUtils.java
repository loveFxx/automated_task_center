package com.sailvan.dispatchcenter.stat.monitor.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.stat.monitor.scheduler.TaskSuccessStatScheduler;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @program: automated_task_center
 * @description: 企业微信机器人工具类
 * @author: Wu Xingjian
 * @create: 2021-08-30 09:57
 **/
public class WeChatRobotUtils {
    private static final Logger logger = LoggerFactory.getLogger(WeChatRobotUtils.class);
    private final String WEBHOOK_TOKEN;

    public WeChatRobotUtils(String token) {
        WEBHOOK_TOKEN = token;
    }

    private String send(String textMsg) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(WEBHOOK_TOKEN);
        httpPost.addHeader("Content-Type", "application/json; charset=utf-8");
        StringEntity se = new StringEntity(textMsg, "utf-8");
        httpPost.setEntity(se);
        CloseableHttpResponse response = httpClient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String result = EntityUtils.toString(response.getEntity(), "utf-8");
            logger.info("发送微信机器人消息成功 " + result);
            return result;
        } else {
            logger.info("发送微信机器人消息失败");
        }
        // 关闭
        httpClient.close();
        response.close();
        return "发送微信机器人消息失败";
    }


    /**
     * 发送text格式的消息
     */
    public String text(String mag, String[] atName, String[] atMobile) throws IOException {
        JSONObject object_text = new JSONObject();
        JSONObject object = new JSONObject();

        object_text.put("content", mag);
        object_text.put("mentioned_list", atName);
        object_text.put("mentioned_mobile_list", atMobile);
        object.put("msgtype", "text");
        object.put("text", object_text);

        return send(String.valueOf(object));
    }

    public String sendMarkdown(String md) throws IOException {
        JSONObject object_text = new JSONObject();
        JSONObject object = new JSONObject();

        object_text.put("content", md);
        object.put("msgtype", "markdown");
        object.put("markdown", object_text);

        return send(String.valueOf(object));
    }


    /**
     * 发送图文格式的消息
     */
    public String pic(String title, String description, String url, String picurl) throws IOException {
        JSONObject object = new JSONObject();
        JSONObject object_news = new JSONObject();
        JSONObject object_articles1 = new JSONObject();

        object_articles1.put("title", title);
        object_articles1.put("description", description);
        object_articles1.put("url", url);
        object_articles1.put("picurl", picurl);

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(object_articles1);

        object_news.put("articles", jsonArray);
        object.put("msgtype", "news");
        object.put("news", object_news);

        return send(String.valueOf(object));
    }

    /**
     * 发送markdown格式的消息
     */
    public String markdown(String string) throws IOException {
        JSONObject object = new JSONObject();
        JSONObject object_markdown = new JSONObject();
        object_markdown.put("content", string);
        object.put("msgtype", "markdown");
        object.put("markdown", object_markdown);
        return send(String.valueOf(object));
    }

    /**
     * 群机器人发消息对齐列
     */

    public String alignColumn(String[][] msgArr) {
        StringBuilder sb = new StringBuilder();
        int[] maxLengthArr = new int[msgArr[0].length];

        for (String[] oneLine : msgArr) {
            for (int i = 0; i < oneLine.length; i++) {
                maxLengthArr[i] = Math.max(maxLengthArr[i], oneLine[i].length());
            }
        }

        for (String[] oneLine : msgArr) {
            for (int i = 0; i < oneLine.length; i++) {
                sb.append(oneLine[i]);
                int lengthDif = maxLengthArr[i] - oneLine[i].length();
                for (int j = 0; j < lengthDif; j++) {
                    //用空格来对齐
                    sb.append(" ");
                }
                //每列之间本来也有一个空格
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }


    public String appendSpace(String str, int maxLength){
        int length = maxLength - str.length();
        StringBuilder strBuilder = new StringBuilder(str);
        for (int i = 0; i<length; i++){
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

}

