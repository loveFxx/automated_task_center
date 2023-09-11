package com.sailvan.dispatchcenter.core.domain;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document(collection = "atc_task_buffer")
public class TaskBuffer implements Serializable {


    @Id
    private String id;

    private String task_source_id;

    private int unique_id;

    private int result_hash_key;

    private String type;

    private String work_type;

    @JsonRawValue
    private String client_params;

    @JsonRawValue
    private String center_params;

    private int priority;

    private int is_enforced;

    private String refresh_time;

    private int retry_times;

    /**
     *  是否入任务池( 0:缓冲区, 1:任务池; 2:出池)
     */
    private int is_in_pool;

    private int in_pool_times; //入池次数

    private int run_mode; //运行模式 0：机器；1:Lambda；2:机器与Lambda；

    private int pool_type = 0; //池子类型 默认0：机器;1:Lambda

    private String in_buffer_time;

    private String in_pool_time;

    private String created_at;

    private String updated_at;
}
