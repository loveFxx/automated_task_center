package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.AwsRegion;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RegionDao {

    List<AwsRegion> getRegionAll();

    AwsRegion getRegionByRegionName(String regionName);

}
