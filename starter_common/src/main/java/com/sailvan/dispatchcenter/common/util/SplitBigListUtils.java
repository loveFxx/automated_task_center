package com.sailvan.dispatchcenter.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: automated_task_center
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-11-12 10:01
 **/
public class SplitBigListUtils {


    /***
     * 递归记录第n个逗号前的子串
     * @param separateNum 多少个逗号一分割
     * @param BigList [1,2,3,4,5,6,7,8,9,10,11]
     * @return ["(1,2,3,4,5)","(6,7,8,9,10)",(11)]
     */
    public static ArrayList<String> BigListToSmallStringLists(List<String> BigList, int separateNum) {

        String joinStr = String.join(",", BigList);
        ArrayList<String> res = new ArrayList<>();

        int ordinalIndexOf = StringUtils.ordinalIndexOf(joinStr, ",", separateNum);
        while (ordinalIndexOf != -1) {
            String subStr1 = joinStr.substring(0, ordinalIndexOf);
            res.add("(" + subStr1 + ")");
            joinStr = joinStr.substring(ordinalIndexOf + 1);
            ordinalIndexOf = StringUtils.ordinalIndexOf(joinStr, ",", separateNum);
        }

        if (joinStr.length() != 0) {
            res.add("(" + joinStr + ")");
        }

        return res;

    }
}
