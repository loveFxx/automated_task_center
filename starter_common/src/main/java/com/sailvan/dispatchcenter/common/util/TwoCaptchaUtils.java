package com.sailvan.dispatchcenter.common.util;

import com.twocaptcha.TwoCaptcha;
import com.twocaptcha.captcha.Normal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.InputStream;



/**
 * @program: automated_task_center
 * @description: 二维码测试
 * @author: Wu Xingjian
 * @create: 2021-06-16 17:23
 **/


public class TwoCaptchaUtils {


    // 地址
    private static final String URL = "https://newtoken.valsun.cn/admin/auth/login";
    // 编码
    private static final String ECODING = "UTF-8";
    // 获取img标签正则
    private static final String IMGURL_REG = "<img.*src=(.*?)[^>]*?>";
    // 获取src路径的正则
    private static final String IMGSRC_REG = "https:\"?(.*?)(\"|>|\\s+)";

    private static final String apiKey = "7282c9a8f61cdab88c001ce075714f34";


    public static void test() throws Exception {

        TwoCaptchaUtils cm = new TwoCaptchaUtils();
        //获得html文本内容
        String HTML = cm.getHTML(URL);
        //获取图片标签
        String imgUrl = cm.getImageUrl(HTML);
        //获取图片src地址
        String imgSrc = cm.getImageSrc(imgUrl);

        //下载图片
        File imageFile = cm.Download(imgSrc);

        TwoCaptcha solver = new TwoCaptcha("7282c9a8f61cdab88c001ce075714f34");
        Normal captcha = new Normal();
        captcha.setFile(imageFile);
        captcha.setMinLen(5);
        captcha.setMaxLen(5);
        captcha.setNumeric(1);


        solver.solve(captcha);
        System.out.println("Captcha solved: " + captcha.getCode());
        imageFile.delete();

    }


    private String getHTML(String url) throws Exception {
        URL uri = new URL(url);

        InputStream is = uri.openStream();
        int ptr = 0;
        StringBuffer sb = new StringBuffer();
        while ((ptr = is.read()) != -1) {
            sb.append((char)ptr);
        }
        return sb.toString();
        
    }


    private String getImageUrl(String HTML) {
        Matcher matcher = Pattern.compile(IMGURL_REG).matcher(HTML);
        String imgUrl = "";
        if (matcher.find()) {
            imgUrl = matcher.group();
        }
        return imgUrl;

    }


    private String getImageSrc(String imageUrl) {
        String imgSrc = "";
        Matcher matcher = Pattern.compile(IMGSRC_REG).matcher(imageUrl);
        if (matcher.find()) {
            imgSrc = matcher.group();
        }

        return imgSrc;
    }


    private File Download(String imgSrc) throws Exception {

        InputStream in = new URL(imgSrc).openStream();
        File returnFile = File.createTempFile("tmp", ".jpg");
        Files.copy(in, Paths.get(returnFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
        in.close();


        return returnFile;
    }

    /**
     *
     * @param base64
     * @param code
     * @return
     * @throws Exception
     */
    public static String getCaptchaCode(String base64,String code, String apiKey) throws Exception{
        String captchaCode = "";
        TwoCaptcha solver = new TwoCaptcha(apiKey);
        Normal captcha = new Normal();
        captcha.setCode(code);
        captcha.setBase64(base64);
        solver.setPollingInterval(1);
        solver.solve(captcha);
        captchaCode = captcha.getCode();
        return captchaCode;
    }

    public static void main(String[] args) throws Exception{
        test();
        System.out.println("Captcha solved: " );
    }


}
