package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.Version;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


/**
 * @author mh
 * @date 2021-07
 */
public interface VersionService {



    public List<Version> getVersionAll() ;

    public List<Version> getValidVersion(int status) ;

    public List<Version> getValidChildVersion(int pid, int status) ;

    public PageDataResult getVersionList(Version version, Integer pageNum, Integer pageSize);


    public int update(Version version);



    public List<Version> getVersionByVersion(Version version);

    public List<Version> getVersionByClientFileVersion(Version version);



    public int insert(Version version);


    public int insertFatherAndSon(Version fatherVersion,Version sonVersion);


    //TODO 事务
    public int deleteVersionById(Version version) throws IOException;


    public Version getVersionById(Integer id);



    public int uploadVersionFile(MultipartFile file, Version version ) throws Exception;



    public List<String> getDropDownVersion();

    /**
     *  此方法没用分页pageInfo 只用到了统一返回的PageDataResult
     */

    public PageDataResult  getVersionFileMap(int versionId) ;


    public int updateVersionFileMap(int id,String clientFilePath,String fileName);



    /**
     *
     * @param fileId 文件的id
     * @param version service层组装路径
     * @return 1：正常删除
     * @return 其他：不正常删除(表里没内容；表里有内容但文件里没内容)
     */
    public int deleteFile(int fileId,String fileName,Version version) throws IOException;

    /**
     * 仅从数据库里删除一条文件配置
     * @return
     * @throws IOException
     */
    public int deleteFileMap(int fileId);


    /**
     * 子版本手动添加map
     * @param versionId
     * @param fileName
     * @param clientFilePath
     * @return
     */
    public int addVersionFileMap(int versionId,String fileName, String clientFilePath);


    Version getChildVersionByVersionId(String clientVersion);

    Version getChildVersionByFileVersionId(String clientFileVersion);
}
