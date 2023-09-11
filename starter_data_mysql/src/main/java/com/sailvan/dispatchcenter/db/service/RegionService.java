package com.sailvan.dispatchcenter.db.service;


import com.sailvan.dispatchcenter.common.domain.AwsRegion;
import com.sailvan.dispatchcenter.db.dao.automated.RegionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yyj
 * @date 2022-02
 */
@Service
public class RegionService implements com.sailvan.dispatchcenter.common.pipe.RegionService {

    @Autowired
    RegionDao regionDao;

    @Override
    public List<AwsRegion> getRegionAll() {
        return regionDao.getRegionAll();
    }

    @Override
    public AwsRegion getRegionByRegionName(String regionName) {
        return regionDao.getRegionByRegionName(regionName);
    }
}
