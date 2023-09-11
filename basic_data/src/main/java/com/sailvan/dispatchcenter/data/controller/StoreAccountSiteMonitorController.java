package com.sailvan.dispatchcenter.data.controller;

import com.sailvan.dispatchcenter.common.domain.StoreAccount;
import com.sailvan.dispatchcenter.common.domain.StoreAccountSites;
import com.sailvan.dispatchcenter.db.dao.automated.StoreAccountDao;
import com.sailvan.dispatchcenter.db.dao.automated.StoreAccountSitesDao;
import com.sailvan.dispatchcenter.shard.dao.TaskSourceListShardDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.*;

/**
 * @program: automated_task_center10
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-11-08 17:25
 **/
@RestController
public class StoreAccountSiteMonitorController {

    @Autowired
    private StoreAccountSitesDao storeAccountSiteDao;

    @Autowired
    private StoreAccountDao storeAccountDao;

    @Autowired
    private TaskSourceListShardDao taskSourceListShardDao;



    /**
     * 账户监控页 所有店铺
     * @return
     * [所有无关联机器店铺的{数量:idList},有任务库无关联机器店铺的{数量:idList},
     * 所有无可用机器店铺的{数量:idList},有任务库无可用机器店铺的{数量:idList},
     * 所有二步验证对不上店铺的{数量:idList},有任务库二步验证对不上店铺的{数量:idList}]
     */
    @RequestMapping(value = "/statAccount")
    public  List<Map<Integer,String>> statAccount() {
        ArrayList<Map<Integer,String>> res=new ArrayList<>();
        List<StoreAccount> list1 = storeAccountDao.getStoreAccountHavingNoMachine();
        List<StoreAccount> list2 = getAccountNoMachineInTaskSrc();
        List<StoreAccount> list3 = storeAccountDao.getStoreAccountHavingNoAvailableMachine();
        List<StoreAccount> list4 = getAccountNoAvailableMachineInTaskSrc();
        List<StoreAccount> list5 = getUsernameNotMatchQrContent();
        List<StoreAccount> list6 = getUsernameNotMatchQrContentInTaskSrc();


        List<List<StoreAccount>> tmpList=Arrays.asList(list1,list2,list3,list4,list5,list6);
        for (List<StoreAccount> list : tmpList) {
            String idList="";
            ArrayList<String> al=new ArrayList<>();
            for (StoreAccount storeAccount : list) {
                al.add(storeAccount.getId());
            }
            idList=String.join(",",al);
            HashMap<Integer, String> map = new HashMap<>();
            map.put(list.size(),idList);
            res.add(map);
        }
        return res;
    }








    /**
     * 账户监控页 把所有站点/有周期任务站点 根据token状态机器状态分类
     * @return
     */
    @RequestMapping(value = "/statAccountSite", method = RequestMethod.POST)


    public List<Map<Integer, Map<String,String>>> statAccountSite() {
        List<StoreAccountSites> sitesList = storeAccountSiteDao.getStoreAccountSitesAll();


        List<Map<Integer, Map<String,String>>> res =new ArrayList<>();

        Map<Integer, Map<String,String>> res1 = countSiteByTokenType(sitesList);


        Map<Integer, Map<String,String>> res2 = countSiteByMachineType(sitesList);

        res.add(res1);
        res.add(res2);


        return res;




    }

    /**
     * 遍历 无机器的店铺 返回其中在周期任务里出现的
     *
     * @return
     */
    public List<StoreAccount> getAccountNoMachineInTaskSrc(){

        List<StoreAccount> list = storeAccountDao.getStoreAccountHavingNoMachine();
        HashSet<String> accountAll = taskSourceListShardDao.getAccountAll();

        ArrayList<StoreAccount> res = new ArrayList<>();
        for (StoreAccount storeAccount : list) {
            String account = storeAccount.getAccount();
            if (accountAll.contains(account)) {
                res.add(storeAccount);
            } else {

            }
        }

        return res;

    }


    /**
     * 遍历 无可用机器的店铺 返回其中在周期任务里出现的
     *
     * @return
     */
    public List<StoreAccount> getAccountNoAvailableMachineInTaskSrc(){

        List<StoreAccount> list = storeAccountDao.getStoreAccountHavingNoAvailableMachine();

        HashSet<String> accountAll = taskSourceListShardDao.getAccountAll();
        ArrayList<StoreAccount> res = new ArrayList<>();
        for (StoreAccount storeAccount : list) {
            String account = storeAccount.getAccount();
            if (accountAll.contains(account)) {
                res.add(storeAccount);
            } else {

            }
        }

        return res;

    }




    /**
     * 全部站点的各token(status_person)的idList
     *
     * @return {status_person:{is1List:"1,2,3",inCircleList:"1,2,3"...}}
     */
    public Map<Integer, Map<String,String>> countSiteByTokenType(List<StoreAccountSites> list) {

        //有周期任务的站点
        HashSet<String> accountSiteInTaskSrc = taskSourceListShardDao.getAccountSiteAll();

        //{1:{id:1,have_machine:3}2:{id:2,have_machine:3}}
        Map<Integer, Map<String,Integer>> havingMachineMap = storeAccountSiteDao.getSiteJoinAccountHavingMachine();


        Map<Integer, Map<String,String>> statusPersonMap = new HashMap<>();
        for (StoreAccountSites site : list) {


            int statusPerson = site.getStatusPerson();
            String params = "{\"account\":\"" + site.getAccount() + "\",\"site\":\"" + site.getSite() + "\"}";
            int haveMachine=havingMachineMap.get(Integer.parseInt(site.getId())).get("have_machine");


            if (!statusPersonMap.containsKey(statusPerson)) {
                HashMap<String,String> emptyMap=new HashMap<>();
                emptyMap.put("inCircleList","");
                emptyMap.put("notInCircleList","");
                emptyMap.put("is1List","");
                emptyMap.put("not1List","");
                emptyMap.put("is0List","");
                emptyMap.put("not0List","");
                statusPersonMap.put(statusPerson,emptyMap);
            }

            if (accountSiteInTaskSrc.contains(params)) {
                statusPersonMap.get(statusPerson).put("inCircleList", statusPersonMap.get(statusPerson).get("inCircleList")+","+site.getId());
            }else{
                statusPersonMap.get(statusPerson).put("notInCircleList", statusPersonMap.get(statusPerson).get("notInCircleList")+","+site.getId());
            }

            if (haveMachine==1) {
                statusPersonMap.get(statusPerson).put("is1List", statusPersonMap.get(statusPerson).get("is1List")+","+site.getId());
            }else{
                statusPersonMap.get(statusPerson).put("not1List", statusPersonMap.get(statusPerson).get("not1List")+","+site.getId());
            }

            if (haveMachine==0) {
                statusPersonMap.get(statusPerson).put("is0List", statusPersonMap.get(statusPerson).get("is1List")+","+site.getId());
            }else{
                statusPersonMap.get(statusPerson).put("not0List", statusPersonMap.get(statusPerson).get("not0List")+","+site.getId());
            }



        }




        return statusPersonMap;
    }


    /**
     * 全部站点的各token(status_machine)状态数
     *
     * @return {status_machine:{总数:2006,有周期任务:1000,无周期任务:1006,无关联机器:1,无可用机器:0}}
     */
    public Map<Integer, Map<String,String>> countSiteByMachineType(List<StoreAccountSites> list ) {

        //有周期任务的站点
        HashSet<String> accountSiteInTaskSrc = taskSourceListShardDao.getAccountSiteAll();

        //{1:{id:1,have_machine:3}2:{id:2,have_machine:3}}
        Map<Integer, Map<String,Integer>> havingMachineMap = storeAccountSiteDao.getSiteJoinAccountHavingMachine();


        Map<Integer, Map<String,String>> statusMachineMap = new HashMap<>();
        for (StoreAccountSites site : list) {


            int statusMachine = site.getStatusMachine();
            String params = "{\"account\":\"" + site.getAccount() + "\",\"site\":\"" + site.getSite() + "\"}";
            int haveMachine=havingMachineMap.get(Integer.parseInt(site.getId())).get("have_machine");


            if (!statusMachineMap.containsKey(statusMachine)) {
                HashMap<String,String> emptyMap=new HashMap<>();
                emptyMap.put("inCircleList","");
                emptyMap.put("notInCircleList","");
                emptyMap.put("is1List","");
                emptyMap.put("not1List","");
                emptyMap.put("is0List","");
                emptyMap.put("not0List","");
                statusMachineMap.put(statusMachine,emptyMap);
            }

            if (accountSiteInTaskSrc.contains(params)) {
                statusMachineMap.get(statusMachine).put("inCircleList", statusMachineMap.get(statusMachine).get("inCircleList")+","+site.getId());
            }else{
                statusMachineMap.get(statusMachine).put("notInCircleList", statusMachineMap.get(statusMachine).get("notInCircleList")+","+site.getId());
            }

            if (haveMachine==1) {
                statusMachineMap.get(statusMachine).put("is1List", statusMachineMap.get(statusMachine).get("is1List")+","+site.getId());
            }else{
                statusMachineMap.get(statusMachine).put("not1List", statusMachineMap.get(statusMachine).get("not1List")+","+site.getId());
            }

            if (haveMachine==0) {
                statusMachineMap.get(statusMachine).put("is0List", statusMachineMap.get(statusMachine).get("is1List")+","+site.getId());
            }else{
                statusMachineMap.get(statusMachine).put("not0List", statusMachineMap.get(statusMachine).get("not0List")+","+site.getId());
            }



        }




        return statusMachineMap;

    }




    /**
     * 所有店铺里 二步验证对不上
     * username和qr_content
     */
    public List<StoreAccount> getUsernameNotMatchQrContent() {
        List<StoreAccount> res = new ArrayList<>();
        List<StoreAccount> storeAccountAll = storeAccountDao.getStoreAccountAll();
        for (StoreAccount storeAccount : storeAccountAll) {
            String qrContent = storeAccount.getQrContent();
            String username = storeAccount.getUsername();
            if (qrContent != null && !qrContent.equals("")) {
                try {
                    URI uri = new URI(qrContent);
                    String path = uri.getPath();
                    path = path.substring(path.indexOf(":") + 1);
                    if (!path.equals(username)) {
                        storeAccount.setQrContent(path);
                        res.add(storeAccount);
                    }
                } catch (Exception e) {
                }
            }
        }
        return res;
    }


    /**
     * 有周期任务库的店铺里 二步验证对不上
     * username和qr_content
     */
    public List<StoreAccount> getUsernameNotMatchQrContentInTaskSrc() {
        List<StoreAccount> res = new ArrayList<>();
        List<StoreAccount> list = getUsernameNotMatchQrContent();
        HashSet<String> accountAll = taskSourceListShardDao.getAccountAll();
        for (StoreAccount storeAccount : list) {
            String account = storeAccount.getAccount();
            if (accountAll.contains(account)) {
                res.add(storeAccount);
            } else {
                System.out.println();

            }
        }
        return res;
    }







}
