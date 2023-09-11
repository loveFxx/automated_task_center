package com.sailvan.dispatchcenter.common.util;

import com.sailvan.dispatchcenter.common.config.FtpConfig;
import com.sailvan.dispatchcenter.common.config.FtpConfigInner;
import kotlin.jvm.Throws;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * @program: automated_task_center2
 * @description: ftpClient工厂
 * @author: Wu Xingjian
 * @create: 2021-10-20 10:04
 **/


public class FTPClientFactory {

    private static Logger logger = LoggerFactory.getLogger(FTPClientFactory.class);

    private  static ArrayList<FTPClient> ftpClientList=new ArrayList<>();

    public static void removeFtpClientFromList(FTPClient ftpClient){
        ftpClientList.remove(ftpClient);
    }

    public  static FTPClient getInstance(Object object)   {

        Thread t = Thread.currentThread();
        String name = t.getName();
        //要返回的实例
        FTPClient resFtpClient=null;
        String host="";
        //在list里找到了
        boolean found=false;
        //获得了可用的
        boolean success=false;
        if (object instanceof FtpConfig) {
            host=((FtpConfig) object).getHost();
        } else if (object instanceof FtpConfigInner) {
            host=((FtpConfigInner)object).getHost();
        }

        logger.info(name+"尝试从list获取FTPClient"+host);

        for (FTPClient client : ftpClientList) {
            if (client.getRemoteAddress().toString().substring(1).equals(host)) {
                logger.info(name+"list找到了实例"+host);
                {
                    found=true;
                    resFtpClient=client;
                }
            }
        }

        //list找到实例后判断是否可用
        if (found) {
            try {
                resFtpClient.sendNoOp();
                success = true;
                //If the FTP server prematurely closes the connection as a result of the client being idle or some other reason causing the server to send FTP reply code 421. This exception may be caught either as an IOException or independently as itself.
            } catch (FTPConnectionClosedException e) {
                success=false;
                logger.info(name+"FTP服务器超时断开了连接."+host);
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                success=false;
                logger.info(name+"sendNoOp请求超时"+host);
                e.printStackTrace();
            } catch (IOException e){
                success=false;
                logger.info(name+"sendNoOp未知错误"+host);
                e.printStackTrace();
            }finally {
                if(!success){
                    try {
                        resFtpClient.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ftpClientList.remove(resFtpClient);
                }
            }
        }else{
            success=false;
            logger.info(name+"list找不到"+"创建FTPClient" +host);
           // resFtpClient = createFTPClientThreeTimes(object);
        }
        //没找到或者找到了但无效 尝试创建三次连接
        if(!success){
            resFtpClient = createFTPClientThreeTimes(object);
        }

        if (resFtpClient!=null) {
            logger.info(name+"成功得到可用的FTP实例"+host);
            return resFtpClient;
        }
        logger.info(name+"没能从list中找到可用的FTP实例,也没能创建成功"+host);
        return null;

    }


    /**
     * 三次调用创建连接
     * @param config
     * @return
     */
    private  static FTPClient createFTPClientThreeTimes(Object config) {
        int times=3;
        for (int i = 0; i < times; i++) {
            FTPClient ftpClient=createFTPClientAndSetCharset(config);
            if (ftpClient != null) {
                return  ftpClient;
            }
        }

        return null;
    }

    /**
     * 创建连接
     * @param config
     * @return FTPClient
     */
    private  static FTPClient createFTPClientAndSetCharset(Object config) {
        String CHARSET = "";
        String host = "";
        String username = "";
        String password = "";
        int port = 21;
        if (config instanceof FtpConfig) {
            FtpConfig ftpConfig = (FtpConfig) config;
            CHARSET = ftpConfig.getCHARSET();
            host = ftpConfig.getHost();
            username = ftpConfig.getUsername();
            password = ftpConfig.getPassword();
            port = ftpConfig.getPort();
        } else if (config instanceof FtpConfigInner) {
            FtpConfigInner ftpConfigInner = (FtpConfigInner) config;
            CHARSET = ftpConfigInner.getCHARSET();
            host = ftpConfigInner.getHost();
            username = ftpConfigInner.getUsername();
            password = ftpConfigInner.getPassword();
            port = ftpConfigInner.getPort();
        }

        try {
            FTPClient ftpClient = new FTPClient();
            //设置ftpClient.connect的超时时间
            ftpClient.setConnectTimeout(15000);
            //传输控制命令的超时时间 对sendNoOp生效
            ftpClient.setDefaultTimeout(15000);
            //设置编码必须在建立连接之前
            ftpClient.setControlEncoding(CHARSET);
            //HTTPTunnelConnector connector = new HTTPTunnelConnector(proxyHost,proxyPort);
            ftpClient.connect(host, port);
            ftpClient.login(username, password);

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                System.out.println(host+"连接FTP失败，用户名或密码错误。");
                ftpClient.disconnect();
                return null;
            } else {
                System.out.println(host+"建立FTP连接成功，加入list!");

                ftpClientList.add(ftpClient);

            }



            return ftpClient;
        } catch (SocketTimeoutException e) {
            System.out.println(host+"创建连接超时");
            e.printStackTrace();
        }catch (Exception e){
            System.out.println(host+"创建连接未知错误");
            e.printStackTrace();
        }
        return null;

    }



}
