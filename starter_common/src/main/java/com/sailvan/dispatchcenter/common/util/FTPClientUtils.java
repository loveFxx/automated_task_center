package com.sailvan.dispatchcenter.common.util;

import com.sailvan.dispatchcenter.common.config.FtpConfig;
import com.sailvan.dispatchcenter.common.config.FtpConfigInner;
import lombok.Data;
import org.apache.commons.net.ftp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * @program: automated_task_center2
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-10-12 16:42
 **/


public class FTPClientUtils {


    public static HashMap<String, LinkedHashMap<String,Integer>> progressMap=new HashMap<String,LinkedHashMap<String,Integer>>();
    //public static int percent;
    //public static String currentUploadFileName;

    private static Logger logger = LoggerFactory.getLogger(FTPClientUtils.class);

    private FTPClientUtils() {
    }


    /**
     * ftp服务器切换工作目录 如果不存在则创建
     */
    private static void switchToDir(FTPClient ftpClient, String dirPath) throws IOException {
        //切换到根目录 否则会从上次的WorkingDirectory相对建立文件夹
        ftpClient.changeWorkingDirectory("/");
        String[] pathElements = dirPath.split("/");
        if (pathElements != null && pathElements.length > 0) {
            for (String singleDir : pathElements) {
                if (!singleDir.equals("")) {
                    boolean existed = ftpClient.changeWorkingDirectory(singleDir);
                    if (!existed) {
                        boolean created = ftpClient.makeDirectory(singleDir);
                        if (created) {
                            System.out.println("ftp服务器创建目录成功" + singleDir);
                            ftpClient.changeWorkingDirectory(singleDir);
                        } else {
                            System.out.println("ftp服务器创建目录失败" + singleDir);

                        }
                    }
                }
            }
        }
    }


    /**
     * 已存在文件夹位置创建storeFileStream 返回outputStream
     *
     * @param
     * @param
     * @throws
     */
    private static OutputStream getOutputStream(FTPClient ftpClient, File file) throws IOException {
        OutputStream outputStream = ftpClient.storeFileStream(file.getName());
        System.out.println("file.getName() = " + file.getName());
        return outputStream;
    }


    private static void writeFileBytes(byte[] bytes, int offset, int length, OutputStream outputStream) throws IOException {
        outputStream.write(bytes, offset, length);
    }

    /**
     *  You must close the OutputStream when you finish writing to it
     *  To finalize the file transfer you must call completePendingCommand
     * @param ftpClient
     * @param outputStream
     * @throws IOException
     */
    private static void finish(FTPClient ftpClient, OutputStream outputStream) throws IOException {
        outputStream.close();
        ftpClient.completePendingCommand();
    }

    private static void UploadByStream(FTPClient ftpClient, File file, OutputStream outputStream,String lockStr) throws IOException {

        //System.out.println(file.getOriginalFilename() + "进入real upload");
        //在传输数据过程中，让传输控制命令的 Socket 假装保持处于工作状态
        ftpClient.setControlKeepAliveTimeout(5000);
        //创建临时文件把MultipartFile转成file
        //File tempFile = File.createTempFile("auto_mated_ftp_upload_tmp_file", "");
        //file.transferTo(tempFile);
        int BUFFER_SIZE = 4096;
        FileInputStream inputStream = new FileInputStream(file);
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        long totalBytesRead = 0;
        int percentCompleted = 0;
        long fileSize = file.length();

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            Thread t = Thread.currentThread();
            String name = t.getName();
            //System.out.println("name=" + name+"上传中"+file.getName());

            //System.out.println(file.getOriginalFilename()+"上传中");
            writeFileBytes(buffer, 0, bytesRead, outputStream);
            totalBytesRead += bytesRead;
            percentCompleted = (int) (totalBytesRead * 100 / fileSize);
           /* percent = percentCompleted;
            currentUploadFileName = file.getName();*/

            //TODO 不为空
            if(progressMap.containsKey(file.getName())){
                progressMap.get(file.getName()).put(lockStr,percentCompleted);

            }else{
                LinkedHashMap<String,Integer> linkedHashMap=new LinkedHashMap<>();
                //percentCompleted可能应该是0
                linkedHashMap.put(lockStr,percentCompleted);
                progressMap.put(file.getName(),linkedHashMap);
            }


            //setProgress(percentCompleted);
        }
        inputStream.close();
        //删除临时文件
        //tempFile.delete();

    }


    /**
     *
     * @return 0获取ftpClient失败
     * @return 1上传成功
     * @return 2上传失败
     *
     */
    public static int upload(String dirPath, File file, Object object)   {

        String lockStr = getStringLockFromConfig(object);


        synchronized (lockStr.intern()) {
            FTPClient ftpClient = FTPClientFactory.getInstance(object);
            if(ftpClient==null){
               return 0;
            }
            Thread t = Thread.currentThread();
            String name = t.getName();
            System.out.println("name=" + name + "进入了FtpClientUtils.upload " + ftpClient.getRemoteAddress().toString().substring(1));

            OutputStream outputStream=null;
            //几个工具类的静态方法 通过参数传递ftpClient实例
            try {
                switchToDir(ftpClient, dirPath);
                outputStream = getOutputStream(ftpClient, file);
                UploadByStream(ftpClient, file, outputStream,lockStr);
            } catch (IOException e) {
                e.printStackTrace();
                return 2;
            } finally {
                try {
                    if(outputStream!=null){
                        finish(ftpClient, outputStream);
                    }
                } catch (IOException e) {
                    //关失败了怎么办 担心这个连接被阻塞了 从连接池移除这个链接 下次获取重新创建
                    FTPClientFactory.removeFtpClientFromList(ftpClient);
                    System.out.println("finish失败了");
                }
            }

        }
        return 1;

    }

    /**
     *
     * @return 0获取ftpClient失败
     * @return 1上传成功
     * @return 2上传失败
     */
    public static int deleteFile(String dirPath, Object object)   {

        String lockStr = getStringLockFromConfig(object);

        synchronized (lockStr.intern()) {

            FTPClient ftpClient = FTPClientFactory.getInstance(object);
            if(ftpClient==null){
                logger.info("deleteFile object:{} is null",object);
                return 0;
            }
            boolean deleted = false;
            try {
                deleted = ftpClient.deleteFile(dirPath);
            } catch (IOException e) {
                e.printStackTrace();
                return 2;
            }
            return 1;
        }
    }

    /**
     * 这里没有加锁
     *
     * @param dirPath
     * @param object
     * @return 0获取ftpClient失败
     * @return 1删除成功
     * @return 2删除失败
     */
    public static int deleteVersionById(String dirPath, Object object)   {

        String lockStr = getStringLockFromConfig(object);
        synchronized (lockStr.intern()) {

            FTPClient ftpClient = FTPClientFactory.getInstance(object);
            if(ftpClient==null){

                return 0;
            }

            try {
                removeFTPDirectory(ftpClient, dirPath, "");
            } catch (IOException e) {
                e.printStackTrace();
                return 2;
            }


            return 1;
        }

    }





    /**
     * 删除ftp非空目录
     */
    private static void removeFTPDirectory(FTPClient ftpClient, String parentDir,
                                           String currentDir) throws IOException {
        String dirToList = parentDir;
        //第一次进来一级目录不会执行 递归时加入二级三级目录
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }
        FTPFile[] subFiles = ftpClient.listFiles(dirToList);
        //空文件夹
        if (subFiles != null && subFiles.length == 0) {
            boolean removed = ftpClient.removeDirectory(dirToList);
            if (removed) {
                System.out.println("REMOVED the directory: " + dirToList);
            } else {
                System.out.println("CANNOT remove the directory: " + dirToList);
            }
        }
        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }
                String filePath = parentDir + "/" + currentDir + "/"
                        + currentFileName;
                if (currentDir.equals("")) {
                    filePath = parentDir + "/" + currentFileName;
                }

                if (aFile.isDirectory()) {
                    // remove the sub directory
                    removeFTPDirectory(ftpClient, dirToList, currentFileName);
                } else {
                    // delete the file
                    boolean deleted = ftpClient.deleteFile(filePath);
                    if (deleted) {
                        System.out.println("DELETED the file: " + filePath);
                    } else {
                        System.out.println("CANNOT delete the file: " + filePath);
                    }
                }
            }

            // finally, remove the directory itself
            boolean removed = ftpClient.removeDirectory(dirToList);
            if (removed) {
                System.out.println("REMOVED the directory: " + dirToList);
            } else {
                System.out.println("CANNOT remove the directory: " + dirToList);
            }
        }
    }

    /**
     * 通过fpt配置中的ip返回字符串锁的字面量
     *
     * @param object
     * @return
     */
    public static String getStringLockFromConfig(Object object) {

        String lockStr = "";

        if (object instanceof FtpConfig) {
            lockStr = ((FtpConfig) object).getHost() + " String lock";
        } else if (object instanceof FtpConfigInner) {
            lockStr = ((FtpConfigInner) object).getHost() + " String lock";
            ;
        }
        return lockStr;

    }
    /**
     * 遍历出路径参数下的所有文件名和文件路径
     * @param ftpConfigInner 创建ftp连接的配置参数
     * @param filePath
     * @return
     * 2022-02 yyj
     */
    public static Map getAllFile(FtpConfigInner ftpConfigInner, String filePath) {
        Map fileNameAndPath = new HashMap();
        try {

            FTPClient ftpClient = FTPClientFactory.getInstance(ftpConfigInner);
            ftpClient.changeWorkingDirectory(filePath);
            FTPFile[] ftpFiles = ftpClient.listFiles();
            if (ftpFiles.length > 0) {
                try {
                    fileNameAndPath = recursion(ftpFiles, ftpClient,"",new HashMap<>());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(fileNameAndPath);
        return fileNameAndPath;
    }

    /**
     * 遍历文件夹
     * @param fileArr
     * @param ftp
     * @param filePath
     * @param fileNameAndPath
     * @return
     * @throws Exception
     */
    private static Map recursion(FTPFile[] fileArr, FTPClient ftp,String filePath,Map fileNameAndPath) throws Exception {

        if (fileArr.length > 0) {
            for (FTPFile it : fileArr) {
                if (it.isDirectory()) {
                    filePath = filePath+"\\"+it.getName();
                    ftp.changeWorkingDirectory(new String(it.getName().getBytes("utf-8"), "iso-8859-1"));
                    FTPFile[] ftpFiles = ftp.listFiles();
                    if (ftpFiles.length > 0) {
                        recursion(ftpFiles, ftp,filePath,fileNameAndPath);
                        ftp.changeToParentDirectory();
                        String[] split = filePath.split("\\\\");
                        filePath = "";
                        for (int i = 1; i < split.length-1 ; i++) {
                            filePath = filePath +"\\"+split[i];
                        }
                    } else {
                        ftp.changeToParentDirectory();  // 空目录务必要返回上一级
                        String[] split = filePath.split("\\\\");
                        filePath = "";
                        for (int i = 1; i < split.length-1 ; i++) {
                            filePath = filePath +"\\"+split[i];
                        }
                    }
                } else {
                    fileNameAndPath.put(filePath+"\\"+it.getName(),it.getName());
                    //System.out.println(filePath);
                }
            }
        }
        return fileNameAndPath;
    }

}
