package com.sailvan.dispatchcenter.common.util;

import com.sailvan.dispatchcenter.common.constant.Constant;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类
 * @author
 * @date
 */
public class CommonUtils {

    final private static Map<String, String> lastSearchMap = new HashMap();

    /**
     * 将字符串转成hash值
     * @param key
     * @return
     */
    public static int toHash(String key) {
        // 数组大小一般取质数
        int arraySize = 11113;
        int hashCode = 0;
        // 从字符串的左边开始计算
        for (int i = 0; i < key.length(); i++) {
            // 将获取到的字符串转换成数字，比如a的码值是97，则97-96=1
            int letterValue = key.charAt(i) - 96;
            // 就代表a的值，同理b=2；
            // 防止编码溢出，对每步结果都进行取模运算
            hashCode = ((hashCode << 5) + letterValue) % arraySize;
        }
        return hashCode;
    }

    public static String firstStrUpperCase(String str) {
        if(StringUtils.isEmpty(str)){
            return "";
        }
        return str.substring(0,1).toUpperCase()+str.substring(1);
    }

    public static String searchInValue(String value){
        if (!StringUtils.isEmpty(value)) {
            value = value.replaceAll(",","' , '");
            return  "( '"+value+"' )";
        }
        return "";
    }

    /**
     *  用来处理 页码在条件改变时,不重置的问题
     * @param searchKey
     * @param cache
     * @return
     */
    public static int getPageNum(String searchKey, String cache){
        if(!lastSearchMap.containsKey(searchKey)){
            lastSearchMap.put(searchKey, cache);
            return 1;
        }else {
            String s = lastSearchMap.get(searchKey);
            if(!s.equals(cache)){
                lastSearchMap.put(searchKey, cache);
                return 1;
            }
        }
        return 0;
    }


    /**
     * 获取格式化后的平台
     * @param platform
     * @return
     */
    public static String getFormatPlatform(String platform){
        if(StringUtils.isEmpty(platform)){
            return "";
        }
        List<String> list = Arrays.asList(Constant.PLATFORMS);
        platform = CommonUtils.firstStrUpperCase(platform);
        if(list.contains(platform)){
            return platform;
        }
        return "";
    }

    public static int hashCode(String prefix, String string) {
        int hashCode = 1;
        hashCode = 31 * hashCode + Objects.hashCode(prefix);
        hashCode = 31 * hashCode + Objects.hashCode(string);
        return hashCode;
    }

    /**
     * 将一个list均分成n个list,主要通过偏移量来实现的
     * @param source
     * @return
     */
    public static <T> List<List<T>> averageAssign(List<T> source,int n){
        List<List<T>> result=new ArrayList<List<T>>();
        int remaider=source.size()%n;  //(先计算出余数)
        int number=source.size()/n;  //然后是商
        int offset=0;//偏移量
        for(int i=0;i<n;i++){
            List<T> value=null;
            if(remaider>0){
                value=source.subList(i*number+offset, (i+1)*number+offset+1);
                remaider--;
                offset++;
            }else{
                value=source.subList(i*number+offset, (i+1)*number+offset);
            }
            result.add(value);
        }
        return result;
    }

    /**
     * 除法运算，保留小数
     * @param a 被除数
     * @param b 除数
     * @return 商
     */
    public static String divide(int a,int b) {
        if (a == 0 || b == 0){
            return "0";
        }
        DecimalFormat df=new DecimalFormat("0.0000");//设置保留几位数
        return df.format((float)a/b);
    }

    public static String match(String string, String regex){
        if (string == null){
            return null;
        }
        Matcher matcher = Pattern.compile(regex).matcher(string);
        String keyword = "";
        // 判断是否可以找到匹配正则表达式的字符
        if (matcher.find()) {
            // 将匹配当前正则表达式的字符串即文件名称进行赋值
            keyword = matcher.group();
        }
        return keyword;
    }

    public static final byte[] input2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }

    //接收2：将byte【】转为16进制
    public static String bytesToHexString(byte[] src) {
        StringBuffer sb = new StringBuffer("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v).toUpperCase();
            if (hv.length() < 2) {
                sb.append(0);
            }
            sb.append(hv);
            if (i != src.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();

    }

    public static ByteArrayOutputStream cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取VC账号区域站点映射
     * @param value
     * @return
     */
    public static String getVCSiteContinent(String value){
        String sites = Constant.VC_SITE_CONTINENT_MAP.get(value);
        if (StringUtils.isEmpty(sites)){
            for (Map.Entry<String,String> entry :  Constant.VC_SITE_CONTINENT_MAP.entrySet()){
                String[] siteArray = entry.getValue().split(",");
                List<String> strings = Arrays.asList(siteArray);
                if (strings.contains(value)){
                    return entry.getKey();
                }
            }
        }else {
            return sites.split(",")[0];
        }
        return "";
    }
}
