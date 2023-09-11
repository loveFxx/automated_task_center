package com.sailvan.dispatchcenter.common.pipe;


import com.sailvan.dispatchcenter.common.domain.AwsRegion;

import java.util.List;

/**
 * @author yyj
 * @date 2022-02
 */

public interface RegionService {

    List<AwsRegion> getRegionAll();

    AwsRegion getRegionByRegionName(String regionName);
}
