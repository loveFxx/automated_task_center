package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.StoreAccountSites;
import com.sailvan.dispatchcenter.db.dao.automated.StoreAccountSitesDao;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mh
 * @date 2021-04
 */
@Service
public class StoreAccountSitesService implements com.sailvan.dispatchcenter.common.pipe.StoreAccountSitesService {
    private static final Logger logger = LoggerFactory.getLogger(StoreAccountSitesService.class);

    @Autowired
    private StoreAccountSitesDao storeAccountSitesDao;


    @Override
    public List<StoreAccountSites> getStoreAccountSitesAll() {
        List<StoreAccountSites> list = storeAccountSitesDao.getStoreAccountSitesAll();
        return list;
    }


    @Override
    public PageDataResult getStoreAccountSitesList(StoreAccountSites storeAccount, Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);

        List<StoreAccountSites> storeAccountSites = storeAccountSitesDao.getStoreAccountSitesByAccountId(storeAccount);
        for (StoreAccountSites storeAccountSite : storeAccountSites) {
            storeAccountSite.setStatusMachineMean(ResponseCode.ERROR_CODE_MAP.get(storeAccountSite.getStatusMachine()));
        }

        PageDataResult pageDataResult = new PageDataResult();
        if(storeAccountSites.size() != 0){
            PageInfo<StoreAccountSites> pageInfo = new PageInfo<>(storeAccountSites);
            pageDataResult.setList(storeAccountSites);
            pageDataResult.setTotals((int) pageInfo.getTotal());
            pageDataResult.setPageNum(pageNum);
        }
        return pageDataResult;
    }

    @Override
    public int update(StoreAccountSites storeAccount){
        return storeAccountSitesDao.updateStoreAccountSites(storeAccount);
    }

    @Override
    public int updateStoreAccountSitesByClient(StoreAccountSites storeAccount){
        return storeAccountSitesDao.updateStoreAccountSitesByClient(storeAccount);
    }

    @Override
    public int updateMachineStatus(StoreAccountSites storeAccount){
        return storeAccountSitesDao.updateMachineStatus(storeAccount);
    }


    @Override
    public int insert(StoreAccountSites storeAccount){
        return storeAccountSitesDao.insertStoreAccountSites(storeAccount);
    }

    @Override
    public List<StoreAccountSites> getStoreAccountSites(StoreAccountSites storeAccount){
        return storeAccountSitesDao.getStoreAccountSites(storeAccount);
    }

    @Override
    public List<StoreAccountSites> getStoreAccountSitesByAccountId(StoreAccountSites storeAccount){
        return storeAccountSitesDao.getStoreAccountSitesByAccountId(storeAccount);
    }

    @Override
    public List<StoreAccountSites> getStoreAccountSitesByAccountSite(StoreAccountSites storeAccount){
        return storeAccountSitesDao.getStoreAccountSitesByAccountSite(storeAccount);
    }

    @Override
    public List<StoreAccountSites> getStoreAccountSitesByAccountContinents(StoreAccountSites storeAccount){
        return storeAccountSitesDao.getStoreAccountSitesByAccountContinents(storeAccount);
    }


    @Override
    public int delete(Integer id){
        return storeAccountSitesDao.deleteStoreAccountSitesById(id);
    }
}
