package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;
import java.io.Serializable;

/**
 * @author mh
 * @date 21-04
 *  客户端机器信息
 */
@Data
public class LambdaUser implements Serializable {

    private Integer id;
    private String accountName;
    private String accessKey;
    private String accessSecret;
    private String lambdaFunction;
    private String region;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

}
