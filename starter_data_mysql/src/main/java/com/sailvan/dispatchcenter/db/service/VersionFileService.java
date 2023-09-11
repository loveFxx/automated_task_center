package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.VersionFile;
import com.sailvan.dispatchcenter.db.dao.automated.VersionFileDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;


/**
 * @author mh
 * @date 2021-07
 */
@Service
public class VersionFileService implements com.sailvan.dispatchcenter.common.pipe.VersionFileService {

    private static Logger logger = LoggerFactory.getLogger(VersionFileService.class);

    @Autowired
    private VersionFileDao versionFileDao;

    @Override
    public List<VersionFile> getVersionFileVersion(int versionId){
        List<VersionFile> list = versionFileDao.getVersionFileVersion(versionId);
        return list;
    }

}
