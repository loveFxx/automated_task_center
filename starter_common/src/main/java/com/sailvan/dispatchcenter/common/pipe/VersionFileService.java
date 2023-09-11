package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.VersionFile;

import java.util.List;


/**
 * @author mh
 * @date 2021-07
 */
public interface VersionFileService {


    public List<VersionFile> getVersionFileVersion(int versionId);

}
