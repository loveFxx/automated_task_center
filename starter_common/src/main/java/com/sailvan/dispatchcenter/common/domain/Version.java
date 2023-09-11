package com.sailvan.dispatchcenter.common.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author mh
 * @date 21-07
 *  版本管理
 */
@Data
public class Version implements Serializable {

    private static final long serialVersionUID = -69571L;

    private int id;

    private int pid;

    private List<VersionFile> versionFile;

    private String clientVersion;

    private String clientFileVersion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String updateTime;

    /**
     * 状态 0禁用  1正常
     */
    private int status = -1;

    /**
     *  更新上限
     */
    private int updateLimit = 1;

    /**
     * 标记是否是全量 1=全量 0=非全量
     */
    private String resetAll;

}

