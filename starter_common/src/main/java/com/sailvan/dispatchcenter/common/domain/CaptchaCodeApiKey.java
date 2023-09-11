package com.sailvan.dispatchcenter.common.domain;


import lombok.Data;

import java.io.Serializable;

/**
 * @author mh
 * @date 21-08
 *  apiKey
 */
@Data
public class CaptchaCodeApiKey implements Serializable {

    private String id;

    private String apiKey;
}
