package com.sailvan.dispatchcenter.db.service;

import com.google.inject.internal.cglib.proxy.$Callback;
import com.sailvan.dispatchcenter.common.config.FtpConfig;
import com.sailvan.dispatchcenter.common.config.FtpConfigInner;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.Version;
import com.sailvan.dispatchcenter.common.util.FTPClientUtils;
import com.sailvan.dispatchcenter.db.dao.automated.VersionDao;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * @author mh
 * @date 2021-07
 */
@Service
public class VersionService implements com.sailvan.dispatchcenter.common.pipe.VersionService {

    private static Logger logger = LoggerFactory.getLogger(VersionService.class);

    @Autowired
    private VersionDao versionDao;

    @Autowired
    FtpConfig ftpConfig;

    @Autowired
    FtpConfigInner ftpConfigInner;


    @Override
    public List<Version> getVersionAll() {
        List<Version> list = versionDao.getVersionAll();
        return list;
    }

    @Override
    public List<Version> getValidVersion(int status) {
        List<Version> list = versionDao.getValidVersion(status);
        return list;
    }

    @Override
    public List<Version> getValidChildVersion(int pid, int status) {
        List<Version> list = versionDao.getValidChildVersion(pid, status);
        return list;
    }

    @Override
    public PageDataResult getVersionList(Version version, Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        //得到所有大版本后 递归查找儿子版本
        List<Version> versionList = versionDao.getVersionByVersion(version);
        List<Version> versionListClone = new ArrayList<>();
        CollectionUtils.addAll(versionListClone, new Object[versionList.size()]);
        Collections.copy(versionListClone, versionList);

        int clientVersionCount = versionDao.getClientVersionCount(version);
        for (Version clientVersion : versionList) {
            List<Version> versionList2 = versionDao.getSonsByFather(clientVersion);
            versionListClone.addAll(versionList2);
        }

        PageDataResult pageDataResult = new PageDataResult();
        if (versionListClone.size() != 0) {
            PageInfo<Version> pageInfoNew = new PageInfo<>(versionListClone);
            pageDataResult.setList(versionListClone);
            //total数目是一级条数
            pageDataResult.setTotals(clientVersionCount);
        }

        return pageDataResult;
    }


    @Override
    public int update(Version version) {
        int result = versionDao.updateVersion(version);
        return result;
    }


    @Override
    public List<Version> getVersionByVersion(Version version) {
        return versionDao.getVersionByVersion(version);

    }

    @Override
    public List<Version> getVersionByClientFileVersion(Version version) {
        return versionDao.getVersionByClientFileVersion(version);

    }

    @Override
    public int insert(Version version) {
        return versionDao.insertVersion(version);
    }

    @Transactional(value="automatedTransactionManager")
    @Override
    public int insertFatherAndSon(Version fatherVersion,Version sonVersion) {

        versionDao.insertVersion(fatherVersion);
        int pid =fatherVersion.getId();
        sonVersion.setPid(pid);
        return versionDao.insertVersion(sonVersion);
    }


    /**
     * 删除一个版本 先删除ftp下的文件夹 数据库中先删除以id为父id的version 再删除id本身
     *
     * @param
     * @return 0 ftpClient连接失败 mysql没删除
     × @return 1 ftp服务器删除成功 mysql删除成功
     * @return 2 删除失败 mysql没删除
     */
    @Override
    public int deleteVersionById(Version version) {

        int id = version.getId();

        int deleted = 0;
        int deletedInner = 0;

        deleted = FTPClientUtils.deleteVersionById(getFtpDirPath(version, ftpConfig.getPath()), ftpConfig);
        logger.info("ftpConfig:{} connection....",ftpConfig);
        if(deleted!=1){
            logger.info("ftpConfig:{} connection error.... deleted:{}",ftpConfig,deleted);
            return deleted;
        }
        logger.info("ftpConfig:{} connection....",ftpConfigInner);
        deletedInner = FTPClientUtils.deleteVersionById(getFtpDirPath(version, ftpConfigInner.getPath()), ftpConfigInner);
        if(deletedInner!=1){
            logger.info("ftpConfig:{} connection error.... deletedInner:{}",ftpConfig,deletedInner);
            return deletedInner;
        }

        //两个服务器上的文件都删除成功
        Version clientVersion = new Version();
        clientVersion.setId(id);
        List<Version> sonVersionsList = versionDao.getSonsByFather(clientVersion);
        for (Version sonVersion : sonVersionsList) {
            versionDao.deleteVersionById(sonVersion.getId());
            versionDao.deleteFileByVersionId(sonVersion.getId());
        }
        versionDao.deleteFileByVersionId(id);
        versionDao.deleteVersionById(id);
        return 1;




    }


    @Override
    public Version getVersionById(Integer id) {
        return versionDao.getVersionById(id);
    }


    /**
     * 上传到ftp服务器，若版本文件夹不存在则创建，然后写入数据库
     *
     * @param file
     * @param version
     * @throws IOException
     * @return 0建立ftp连接失败
     * @return 1上传成功
     * @return 2上传失败
     */
    @Override
    public int uploadVersionFile(MultipartFile file, Version version) {


        Thread curT = Thread.currentThread();
        String name = curT.getName();
        System.out.println("name=" + name + "进入了service uploadVersionFile");


        String fileName = file.getOriginalFilename();
        File tempFile = new File(new File(file.getOriginalFilename()).getAbsolutePath());

        try {
            file.transferTo(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int res1=0;
        int res2=0;

        //上传重试3次
        for (int i = 0; i < 3; i++) {
            res1 = FTPClientUtils.upload(getFtpDirPath(version, ftpConfigInner.getPath()), tempFile, ftpConfigInner);
            if (res1 == 1) {
                break;
            }

        }
        if(res1!=1){
            return res1;
        }


        //同一ip的连接不同文件同时上传会在upload被阻塞
        for (int i = 0; i < 3; i++) {
            res2 = FTPClientUtils.upload(getFtpDirPath(version, ftpConfig.getPath()), tempFile, ftpConfig);
            if (res2 == 1) {
                break;
            }
        }
        if(res2!=1){
            return res2;
        }
        tempFile.delete();
        versionDao.insertVersionFile(version.getId(), fileName);
        return 1;
    }


    @Override
    public List<String> getDropDownVersion() {
        return versionDao.getBigVersion();
    }


    /**
     * 此方法没用分页pageInfo 只用到了统一返回的PageDataResult
     */
    @Override
    public PageDataResult getVersionFileMap(int versionId) {
        List<Map<String, String>> versionFileMap = versionDao.getVersionFileMap(versionId);
        PageDataResult pageDataResult = new PageDataResult();
        pageDataResult.setList(versionFileMap);
        return pageDataResult;
    }


    @Override
    public int updateVersionFileMap(int id, String clientFilePath,String fileName) {
        return versionDao.updateVersionFileMap(id, clientFilePath,fileName);
    }


    /**
     * 先删文件后删表
     *
     * @return 0获取ftpClient失败
     * @return 1上传成功
     * @return 2上传失败
     */
    @Override
    public int deleteFile(int fileId, String fileName, Version version) {

        int deleted1 = 0;
        int deleted2 = 0;
        deleted1=FTPClientUtils.deleteFile(getFtpDirPath(version, ftpConfig.getPath()) + File.separator + fileName, ftpConfig);
        if(deleted1!=1){
            return deleted1;
        }
        deleted2=FTPClientUtils.deleteFile(getFtpDirPath(version, ftpConfigInner.getPath()) + File.separator + fileName, ftpConfigInner);
        if(deleted2!=1){
            return deleted2;
        }

        versionDao.deleteFile(fileId);
        return 1;
    }



    @Override
    public int deleteFileMap(int fileId) {

        return versionDao.deleteFile(fileId);

    }


    public static String getFtpDirPath(Version version, String path) {

        String dirPath;
        if (StringUtils.isEmpty(version.getClientFileVersion())) {
            //父亲版本
            dirPath = path + File.separator + Constant.VERSION_FILE + File.separator + version.getClientVersion();
        } else {
            //子级版本
            dirPath = path + File.separator + Constant.VERSION_FILE + File.separator + version.getClientVersion() + File.separator + version.getClientFileVersion();
        }

        return dirPath;

    }


    @Override
    public int addVersionFileMap(int versionId,String fileName, String clientFilePath) {
        return versionDao.addVersionFileMap(versionId, fileName,clientFilePath);
    }

    @Override
    public Version getChildVersionByVersionId(String clientVersion) {
        return versionDao.getChildVersionByVersionId(clientVersion);
    }

    @Override
    public Version getChildVersionByFileVersionId(String clientFileVersion) {
        return versionDao.getChildVersionByFileVersionId(clientFileVersion);
    }

}
