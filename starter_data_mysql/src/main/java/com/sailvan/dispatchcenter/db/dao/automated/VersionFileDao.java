package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.VersionFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mh
 * @date 21-07
 */
@Mapper
public interface VersionFileDao {

    /**
     * 最新有效的子版本
     * @param versionId
     * @return
     */
    List<VersionFile> getVersionFileVersion(@Param("versionId") int versionId);

}
