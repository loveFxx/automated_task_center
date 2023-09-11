package com.sailvan.dispatchcenter.core.util;

import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.core.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;

/**
 * 收费验证码
 */
public class BingTopDaMa {

    private static Logger logger = LoggerFactory.getLogger(BingTopDaMa.class);

    public static void main(String[] args) {
        // 读取图片base64编码数据
//        String captchaData = bingtopDama.getImageStr("图片路径.jpg");
        String captchaData = "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";
        System.out.println(getCode(captchaData));
    }

    public static String getCode(String captchaData){
        // post图片数据，账号信息，进行打码
        String returnStr = BingTopDaMa.sendPost(
                "http://www.bingtop.com/ocr/upload/",
                "username=bt123456&password=btsw987654&captchaType=1001&captchaData="+captchaData,"UTF-8"
        );
        JSONObject jsonObject = JSONObject.parseObject(returnStr);
        if ("0".equals(String.valueOf(jsonObject.get("code")))) {
            JSONObject data = (JSONObject)jsonObject.get("data");
            if(!data.containsKey("recognition") || data.get("recognition") == null){
                logger.error("BingTopDaMa parse recognition error:{}",jsonObject.toJSONString());
                return "";
            }
            return String.valueOf(data.get("recognition"));
        }
        logger.error("BingTopDaMa parse error:{}",jsonObject.toJSONString());
        return jsonObject.toJSONString();
    }

    public static String getImageStr(String imgFile) {
        /**
         * @Description: 根据图片地址转换为base64编码字符串
         * @return String
         */
        FileInputStream inputStream = null;
        byte[] data = null;
        try {
            inputStream = new FileInputStream(imgFile);
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Encoder encoder = Base64.getEncoder();
        String encodedata = encoder.encodeToString(data);
        String encode="";
        try {
            encode = URLEncoder.encode(encodedata,"UTF-8");
        } catch (Exception e) {
            System.out.println("转码异常!"+e);
            e.printStackTrace();
        }
        return encode;
    }


    public static String sendPost(String url, String param,String charset) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        String line;
        StringBuffer sb=new StringBuffer();
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性 设置请求格式
            conn.setRequestProperty("contentType", charset);
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            //设置超时时间
            conn.setConnectTimeout(60*1000);
            conn.setReadTimeout(60*1000);

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),"UTF-8"));
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应    设置接收格式
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(),charset));
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            result=sb.toString();
        } catch (Exception e) {
            System.out.println("发送 POST请求出现异常!"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }
}