package com.sailvan.dispatchcenter.es.util;

import com.alibaba.fastjson.JSON;
import com.sailvan.dispatchcenter.es.config.EsMarkerConfiguration;
import org.apache.commons.codec.binary.Base64;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * ES 工具类，用来插入或更新es使用
 * @date 2022-03
 * @author menghui
 */
@ConditionalOnBean(EsMarkerConfiguration.EsMarker.class)
public class EsUtils {

    private static Logger logger = LoggerFactory.getLogger(EsUtils.class);

    private static ConcurrentMap<String, Map<String, String>> esFieldTypes = new ConcurrentHashMap<>();

    @Autowired
    ESConnection esConnection;

    public Object getValFromMap(String index,String doc, Map<String, Object> resultSet, String fieldName,
                               String columnName)  {
        fieldName = cleanColumn(fieldName);
        columnName = cleanColumn(columnName);
        String esType = getEsType(index, doc, fieldName);
        Object value = resultSet.get(columnName);
        return typeConvert(value, esType);

    }


    /**
     * 类型转换为Mapping中对应的类型
     */
    public static Object typeConvert(Object val, String esType) {
        if (val == null) {
            return null;
        }
        if (esType == null) {
            return val;
        }
        Object res = null;
        if ("integer".equals(esType)) {
            if (val instanceof Number) {
                res = ((Number) val).intValue();
            } else {
                res = Integer.parseInt(val.toString());
            }
        } else if ("long".equals(esType)) {
            if (val instanceof Number) {
                res = ((Number) val).longValue();
            } else {
                res = Long.parseLong(val.toString());
            }
        } else if ("short".equals(esType)) {
            if (val instanceof Number) {
                res = ((Number) val).shortValue();
            } else {
                res = Short.parseShort(val.toString());
            }
        } else if ("byte".equals(esType)) {
            if (val instanceof Number) {
                res = ((Number) val).byteValue();
            } else {
                res = Byte.parseByte(val.toString());
            }
        } else if ("double".equals(esType)) {
            if (val instanceof Number) {
                res = ((Number) val).doubleValue();
            } else {
                res = Double.parseDouble(val.toString());
            }
        } else if ("float".equals(esType) || "half_float".equals(esType) || "scaled_float".equals(esType)) {
            if (val instanceof Number) {
                res = ((Number) val).floatValue();
            } else {
                res = Float.parseFloat(val.toString());
            }
        } else if ("boolean".equals(esType)) {
            if (val instanceof Boolean) {
                res = val;
            } else if (val instanceof Number) {
                int v = ((Number) val).intValue();
                res = v != 0;
            } else {
                res = Boolean.parseBoolean(val.toString());
            }
        } else if ("date".equals(esType)) {
            if (val instanceof java.sql.Time) {
                DateTime dateTime = new DateTime(((java.sql.Time) val).getTime());
                if (dateTime.getMillisOfSecond() != 0) {
                    res = dateTime.toString("HH:mm:ss.SSS");
                } else {
                    res = dateTime.toString("HH:mm:ss");
                }
            } else if (val instanceof java.sql.Timestamp) {
                DateTime dateTime = new DateTime(((java.sql.Timestamp) val).getTime());
                if (dateTime.getMillisOfSecond() != 0) {
                    res = dateTime.toString("yyyy-MM-dd'T'HH:mm:ss.SSS" + DateUtil.timeZone);
                } else {
                    res = dateTime.toString("yyyy-MM-dd'T'HH:mm:ss" + DateUtil.timeZone);
                }
            } else if (val instanceof java.sql.Date || val instanceof Date) {
                DateTime dateTime;
                if (val instanceof java.sql.Date) {
                    dateTime = new DateTime(((java.sql.Date) val).getTime());
                } else {
                    dateTime = new DateTime(((Date) val).getTime());
                }
//                if (dateTime.getHourOfDay() == 0 && dateTime.getMinuteOfHour() == 0 && dateTime.getSecondOfMinute() == 0
//                        && dateTime.getMillisOfSecond() == 0) {
//                    res = dateTime.toString("yyyy-MM-dd");
//                } else {
//                    if (dateTime.getMillisOfSecond() != 0) {
//                        res = dateTime.toString("yyyy-MM-dd'T'HH:mm:ss.SSS" + DateUtil.timeZone);
//                    } else {
                res = dateTime.toString("yyyy-MM-dd'T'HH:mm:ss" + DateUtil.timeZone);
//                    }
//                }
            } else if (val instanceof Long) {
                DateTime dateTime = new DateTime(((Long) val).longValue());
                if (dateTime.getHourOfDay() == 0 && dateTime.getMinuteOfHour() == 0 && dateTime.getSecondOfMinute() == 0
                        && dateTime.getMillisOfSecond() == 0) {
                    res = dateTime.toString("yyyy-MM-dd");
                } else if (dateTime.getMillisOfSecond() != 0) {
                    res = dateTime.toString("yyyy-MM-dd'T'HH:mm:ss.SSS" + DateUtil.timeZone);
                } else {
                    res = dateTime.toString("yyyy-MM-dd'T'HH:mm:ss" + DateUtil.timeZone);
                }
            } else if (val instanceof String) {
                String v = ((String) val).trim();
                if (v.length() > 18 && v.charAt(4) == '-' && v.charAt(7) == '-' && v.charAt(10) == ' '
                        && v.charAt(13) == ':' && v.charAt(16) == ':') {
                    String dt = v.substring(0, 10) + "T" + v.substring(11);
                    Date date = DateUtil.parseDate(dt);
                    if (date != null) {
                        DateTime dateTime = new DateTime(date);
                        if (dateTime.getMillisOfSecond() != 0) {
                            res = dateTime.toString("yyyy-MM-dd'T'HH:mm:ss.SSS" + DateUtil.timeZone);
                        } else {
                            res = dateTime.toString("yyyy-MM-dd'T'HH:mm:ss" + DateUtil.timeZone);
                        }
                    }
                } else if (v.length() == 10 && v.charAt(4) == '-' && v.charAt(7) == '-') {
                    Date date = DateUtil.parseDate(v);
                    if (date != null) {
                        DateTime dateTime = new DateTime(date);
                        res = dateTime.toString("yyyy-MM-dd");
                    }
                }
            }
        } else if ("binary".equals(esType)) {
            if (val instanceof byte[]) {
                Base64 base64 = new Base64();
                res = base64.encodeAsString((byte[]) val);
            } else if (val instanceof Blob) {
                byte[] b = blobToBytes((Blob) val);
                Base64 base64 = new Base64();
                res = base64.encodeAsString(b);
            } else if (val instanceof String) {
                // 对应canal中的单字节编码
                byte[] b = ((String) val).getBytes(StandardCharsets.ISO_8859_1);
                Base64 base64 = new Base64();
                res = base64.encodeAsString(b);
            }
        } else if ("geo_point".equals(esType)) {
            if (!(val instanceof String)) {
                logger.error("es type is geo_point, but source type is not String");
                return val;
            }

            if (!((String) val).contains(",")) {
                logger.error("es type is geo_point, source value not contains ',' separator");
                return val;
            }

            String[] point = ((String) val).split(",");
            Map<String, Double> location = new HashMap<>();
            location.put("lat", Double.valueOf(point[0].trim()));
            location.put("lon", Double.valueOf(point[1].trim()));
            return location;
        } else if ("array".equals(esType)) {
            if ("".equals(val.toString().trim())) {
                res = new ArrayList<>();
            } else {
                String value = val.toString();
                String separator = ",";
                if (!value.contains(",")) {
                    if (value.contains(";")) {
                        separator = ";";
                    } else if (value.contains("|")) {
                        separator = "\\|";
                    } else if (value.contains("-")) {
                        separator = "-";
                    }
                }
                String[] values = value.split(separator);
                return Arrays.asList(values);
            }
        } else if ("object".equals(esType)) {
            if ("".equals(val.toString().trim())) {
                res = new HashMap<>();
            } else {
                res = JSON.parseObject(val.toString(), Map.class);
            }
        } else {
            // 其他类全以字符串处理
            res = val.toString();
        }

        return res;
    }

    /**
     * Blob转byte[]
     */
    private static byte[] blobToBytes(Blob blob) {
        try (InputStream is = blob.getBinaryStream()) {
            byte[] b = new byte[(int) blob.length()];
            if (is.read(b) != -1) {
                return b;
            } else {
                return new byte[0];
            }
        } catch (IOException | SQLException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    private String getEsType(String index,String doc, String fieldName) {
        String key = index + "-" + doc;
        Map<String, String> fieldType = esFieldTypes.get(key);
        if (fieldType != null) {
            return fieldType.get(fieldName);
        } else {
            MappingMetaData mappingMetaData = esConnection.getMapping(index, doc);

            if (mappingMetaData == null) {
                throw new IllegalArgumentException("Not found the mapping info of index: " + index);
            }

            fieldType = new LinkedHashMap<>();

            Map<String, Object> sourceMap = mappingMetaData.getSourceAsMap();
            Map<String, Object> esMapping = (Map<String, Object>) sourceMap.get("properties");
            for (Map.Entry<String, Object> entry : esMapping.entrySet()) {
                Map<String, Object> value = (Map<String, Object>) entry.getValue();
                if (value.containsKey("properties")) {
                    fieldType.put(entry.getKey(), "object");
                } else {
                    fieldType.put(entry.getKey(), (String) value.get("type"));
                }
            }
            esFieldTypes.put(key, fieldType);

            return fieldType.get(fieldName);
        }
    }

    public static String cleanColumn(String column) {
        if (column == null) {
            return null;
        }
        if (column.contains("`")) {
            column = column.replaceAll("`", "");
        }

        if (column.contains("'")) {
            column = column.replaceAll("'", "");
        }

        if (column.contains("\"")) {
            column = column.replaceAll("\"", "");
        }

        return column;
    }


    /**
     *  用来匹配是否符合规则,目前仅支持时间后缀
     * @param regular
     * @param format
     * @return
     */
    public static boolean regularFormat(String regular, String format){
        if(org.apache.commons.lang.StringUtils.isEmpty(format)){
            return false;
        }
        if("yyyyMMdd".equals(format) || "yyyyMM".equals(format) || "yyyy".equals(format)){
            Date date = convertDate(regular, format);
            if(date != null){
                return true;
            }
        }
        return false;
    }

    public static Date convertDate(String time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            return null;
        }
        return date;
    }
}
