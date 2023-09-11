package com.sailvan.dispatchcenter.common.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;


/**
 * @author mh
 * @date 21-09
 *  平台信息
 */
@Data
public class Platform implements Serializable {

    private int id;

    /**
     *  英文名 为了存储到数据库里面(存储的也可能是ID)
     */
    private String platformName;

    /**
     *  中文名 为了显示在前端
     */
    private String platformNameZh;

    private int isBrowser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String updatedAt;

    private String config;

    /**
     * 0:独立（拥有独有的代理IP池） 1:可共享 （只能挂共享的代理IP）2:店铺及可共享（除店铺自己的代理IP，也可挂共享的代理IP）3：店铺及不可共享（只挂店铺本身的代理IP）
     */
    private int relatedProxyIp;
}
