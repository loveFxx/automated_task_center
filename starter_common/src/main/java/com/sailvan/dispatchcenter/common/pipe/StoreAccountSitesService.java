package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.StoreAccountSites;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

import java.util.List;

/**
 * @author mh
 * @date 2021-04
 */
public interface StoreAccountSitesService {



    public List<StoreAccountSites> getStoreAccountSitesAll();


    public PageDataResult getStoreAccountSitesList(StoreAccountSites storeAccount, Integer pageNum, Integer pageSize);
    public int update(StoreAccountSites storeAccount);

    public int updateStoreAccountSitesByClient(StoreAccountSites storeAccount);

    public int updateMachineStatus(StoreAccountSites storeAccount);


    public int insert(StoreAccountSites storeAccount);

    public List<StoreAccountSites> getStoreAccountSites(StoreAccountSites storeAccount);
    public List<StoreAccountSites> getStoreAccountSitesByAccountId(StoreAccountSites storeAccount);
    public List<StoreAccountSites> getStoreAccountSitesByAccountSite(StoreAccountSites storeAccount);
    public List<StoreAccountSites> getStoreAccountSitesByAccountContinents(StoreAccountSites storeAccount);


    public int delete(Integer id);
}
