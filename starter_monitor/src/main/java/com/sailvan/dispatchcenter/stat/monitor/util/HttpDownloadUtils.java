package com.sailvan.dispatchcenter.stat.monitor.util;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @program: automated_task_center
 * @description:
 * @author: Wu Xingjian
 * @create: 2022-01-06 17:58
 **/


public class HttpDownloadUtils {


    public  static String httpDownload(HttpServletResponse response, File file ){

        //response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "attachment;filename=" + file.getName());
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));) {
            byte[] buff = new byte[1024];
            OutputStream os = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            return "下载失败";
        }
        return "下载成功";
    }
}
