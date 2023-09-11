package com.sailvan.dispatchcenter.es.config;

import com.sailvan.dispatchcenter.es.domain.Table;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * map映射，主要是es字段和对应的实体类之间的对应关系
 * 如果需要更新或新增es文档，便会用到这个
 * 如果只是查询 这个类没用
 * @date 2022-03
 * @author menghui
 */
@Component
@PropertySource(value = "classpath:fields_map/table_es_map.yml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "es-config")
@ConditionalOnBean(EsMarkerConfiguration.EsMarker.class)
public class MapConfig {

    private List<Table> list;

    public List<Table> getList() {
        return list;
    }

    public void setList(List<Table> list) {
        this.list = list;
    }
}
