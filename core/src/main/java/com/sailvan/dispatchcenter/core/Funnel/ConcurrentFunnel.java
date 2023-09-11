package com.sailvan.dispatchcenter.core.Funnel;

import com.sailvan.dispatchcenter.common.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据各个任务配置的任务生成速率生成相应规格的漏斗来控制
 */
public class ConcurrentFunnel {

    public static ConcurrentHashMap<String, TypeFunnel> hashFunnel= new ConcurrentHashMap<>();

    static public class TypeFunnel {

        public final ConcurrentHashMap<Integer,TypeFunnelList> typeFunnelLists = new ConcurrentHashMap<>();

        private int capacity = 500;

        private int seconds = 30;

        private long lastLeakingTime = System.currentTimeMillis();

        private String expectedTime = DateUtils.getCurrentDate();

        static public class TypeFunnelList{

            private String jobName;

            volatile List<Integer> lists = new ArrayList<>();

            public void setJobName(String jobName) {
                this.jobName = jobName;
            }

            public String getJobName() {
                return jobName;
            }

            public void push(List<Integer> ids){
                lists.addAll(ids);
            }

            public List<Integer> getLists() {
                return lists;
            }
        }

        public void init(){
            this.lastLeakingTime = System.currentTimeMillis();
            this.typeFunnelLists.clear();
        }

        public boolean checkIsSingle(){
            //默认以第一个建立的漏斗为标准
            if (!typeFunnelLists.isEmpty()){
                TypeFunnelList typeFunnelList = typeFunnelLists.get(1);
                if (typeFunnelList.lists.size() == capacity || System.currentTimeMillis() - lastLeakingTime > (seconds - 2) * 1000L) {
                    return true;
                }
            }
            return false;
        }

        public List<Integer> getLists(){
            //默认以第一个建立的漏斗为标准
            if (!typeFunnelLists.isEmpty()){
                TypeFunnelList typeFunnelList = typeFunnelLists.get(1);
                return typeFunnelList.lists;
            }
            return null;
        }

        public void setExpectedTime(String expectedTime) {
            this.expectedTime = expectedTime;
        }

        public String getExpectedTime() {
            return expectedTime;
        }

        public void setSeconds(int seconds) {
            this.seconds = seconds;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }
    }

    public TypeFunnel checkTypeFunnel(String type){
        TypeFunnel typeFunnel = hashFunnel.get(type);
        if (typeFunnel == null){
            typeFunnel =  new TypeFunnel();
            hashFunnel.put(type,typeFunnel);
        }
        return typeFunnel;
    }
}
