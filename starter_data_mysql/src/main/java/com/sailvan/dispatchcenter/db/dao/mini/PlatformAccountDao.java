package com.sailvan.dispatchcenter.db.dao.mini;

import com.sailvan.dispatchcenter.common.domain.PlatformAccount;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author mini
 * @date 21-05
 *
 *  mini
 */
@Mapper
public interface PlatformAccountDao {

    /**
     * 查询mini所有
     * @return
     */
    List<PlatformAccount> getPlatformAccountAll(List<String> platformList);

}
