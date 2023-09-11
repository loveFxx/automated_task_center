package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.Version;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author mh
 * @date 21-07
 */
@Mapper
public interface VersionDao {

    /**
     * 搜索所有
     * @return
     */
    List<Version> getVersionAll();

    /**
     * 最新有效的版本
     * @param status
     * @return
     */
    List<Version> getValidVersion(@Param("status") int status);

    /**
     * 最新有效的子版本
     * @param status
     * @return
     */
    List<Version> getValidChildVersion(@Param("pid") int pid, @Param("status") int status);


    /**
     *  根据Id获取
     * @param id
     * @return
     */
    Version getVersionById(@Param("id") int id);

    /**
     *  根据指定个别参数搜索 返回顺序根据大版本+小版本
     * @param Version
     * @return
     */
    List<Version> getVersionByVersion(Version Version);//得到了所有的大版本



    /**
     *  根据pid查小版本
     * @param Version
     * @return
     */
    List<Version> getSonsByFather(Version Version);



    /**
     *  获得一级版本总数 给getVersionByVersion计数
     * @param Version
     * @return
     */
    int getClientVersionCount(Version Version);


    /**
     *  根据ClientFileVersion(小版本)搜索
     * @param Version
     * @return
     */
    List<Version> getVersionByClientFileVersion(Version Version);

    /**
     *  更新
     * @param version
     * @return
     */
    int updateVersion(Version version);





    /**
     *  插入
     * @param version
     * @return
     */
    int insertVersion(Version version);


    /**
     *  删除
     * @param id
     * @return
     */
    int deleteVersionById(Integer id);


    List<String> getBigVersion();

    List<Map<String, String>> getVersionFileMap(int versionId);

    int updateVersionFileMap(int id, String clientFilePath,String fileName);


    /***
     * 根据fileMap表的id删除file
     * @param id
     * @return
     */
    int deleteFile(int id);

    /***
     * 根据vesion的id删除file
     * @param versionId
     * @return
     */
    int deleteFileByVersionId(int versionId);


    /**
     *  插入映射 上传时用
     * @param
     * @return
     */
    int insertVersionFile(int versionId,String fileName);

    /**
     * 同样插入映射 手动添加时用
     * @param versionId
     * @param fileName
     * @param clientFilePath
     * @return
     */
    int addVersionFileMap(int versionId,String fileName, String clientFilePath);


    Version getChildVersionByVersionId(String clientVersion);

    Version getChildVersionByFileVersionId(String clientFileVersion);
}
