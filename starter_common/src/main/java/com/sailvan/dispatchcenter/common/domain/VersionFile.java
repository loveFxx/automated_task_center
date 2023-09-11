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
public class VersionFile implements Serializable {

    private int id;

    private int versionId;

    private String fileName;

    private String clientFilePath;

}

