package com.sailvan.dispatchcenter.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author gzj
 * reids 操作类
 */
@Component
public class RedisUtils {
    private static Logger logger = LoggerFactory.getLogger(RedisUtils.class);
    @Resource
    private RedisTemplate redisTemplate;


    /**
     * 写入缓存
     * @param key 键名
     * @param value 键值
     * @param sortKey 排序字段
     * @return 布尔类型
     */
    public Boolean add(Object key, Object value, double sortKey){
        try {
            ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            return zSetOperations.add(key, value, sortKey);
        } catch (Exception e) {
            logger.error("redis add缓存失败！rediskey--{},错误信息为：{}" ,key, e.getMessage());
        }
        return false;
    }

    /**
     * 获取选择区间元素
     * @param key  键名
     * @param start 起始位置
     * @param end  截止位置
     * @return 集合
     */
    public Set range(Object key, long start, long end){
        try {
            ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            return zSetOperations.range(key, start, end);
        } catch (Exception e) {
            logger.error("redis range缓存失败！rediskey--{},错误信息为：{}" ,key, e.getMessage());
        }
        return null;
    }

    /**
     * 返回指定值的分数
     * @param key
     * @param value
     * @return
     */
    public Double score(Object key, Object value){
        try {
            ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            return zSetOperations.score(key, value);
        } catch (Exception e) {
            logger.error("redis range缓存失败！rediskey--{},错误信息为：{}" ,key, e.getMessage());
        }
        return null;
    }

    public Set revrange(Object key, long start, long end){
        try {
            ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            return zSetOperations.reverseRange(key, start, end);
        } catch (Exception e) {
            logger.error("redis revrange缓存失败！rediskey--{},错误信息为：{}" ,key, e.getMessage());
        }
        return null;
    }

    /**
     * 获取选择区间的元素，可将分值都带出来
     * @param key  键名
     * @param start 起始位置
     * @param end  截止位置
     * @return 集合
     */
    public Set rangeWithScores(Object key, long start, long end){
        try {
            ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            return zSetOperations.rangeWithScores(key, start, end);
        } catch (Exception e) {
            logger.error("redis rangeWithScores缓存失败！rediskey--{},错误信息为：{}" ,key, e.getMessage());
        }
        return null;
    }

    /**
     * 根据选择区间删除元素
     * @param key 键名
     * @param start 起始位置
     * @param end 截止位置
     */
    public void removeRange(Object key, long start, long end){
        try {
            ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            zSetOperations.removeRange(key, start, end);
        } catch (Exception e) {
            logger.error("redis removeRange缓存失败！rediskey--{},错误信息为：{}" ,key, e.getMessage());
        }
    }

    public void remove(Object key, Object value){
        try {
            ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            zSetOperations.remove(key,value);
        } catch (Exception e) {
            logger.error("redis zset remove缓存失败！rediskey--{},错误信息为：{}" ,key, e.getMessage());
        }
    }

    /**
     * 写入redis缓存（设置expire存活时间）
     * @param key
     * @param value
     * @param expire
     * @return
     */
    public boolean put(final String key, String value, Long expire){
        boolean result = false;
        try {
            ValueOperations operations = redisTemplate.opsForValue();
            operations.set(key, value);
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            logger.error("redis put缓存失败！rediskey--{},错误信息为：{}" ,key, e.getMessage());
        }
        return result;
    }


    /**
     * 读取redis缓存
     * @param key
     * @return
     */
    public Object get(final String key){
        Object result = null;
        try {
            ValueOperations operations = redisTemplate.opsForValue();
            result = operations.get(key);
        } catch (Exception e) {
            logger.error("redis get缓存失败！rediskey--{},错误信息为：{}" ,key, e.getMessage());
        }
        return result;
    }

    /**
     * 判断redis缓存中是否有对应的key
     * @param key
     * @return
     */
    public boolean exists(final String key){
        boolean result = false;
        try {
            result = redisTemplate.hasKey(key);
        } catch (Exception e) {
            logger.error("判断redis缓存中是否有对应的key失败！rediskey--{},错误信息为：{}" ,key, e.getMessage());
        }
        return result;
    }

    /**
     * redis根据key删除对应的value
     * @param key
     * @return
     */
    public boolean remove(final String key){
        boolean result = false;
        try {
            if(exists(key)){
                redisTemplate.delete(key);
            }
            result = true;
        } catch (Exception e) {
            logger.error("redis remove缓存失败！rediskey--{},错误信息为：{}" ,key, e.getMessage());
        }
        return result;
    }

    public long lPush(Object key, Object value) {
        ListOperations listOperations = redisTemplate.opsForList();
        return listOperations.leftPush(key, value);
    }

    public Object rPop(Object key){
        ListOperations listOperations = redisTemplate.opsForList();
        return listOperations.rightPop(key);
    }
}
