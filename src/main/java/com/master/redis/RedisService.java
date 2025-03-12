package com.master.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RedisService {
    private static final String PREFIX_KEY_POS = "pos:";
    private static final String PREFIX_KEY_VIEW = "view:";
    private static final String PREFIX_KEY_EMPLOYEE = "employee:";
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public String getKeyString(Integer keyType, String tenantName, String posId) {
        if (RedisConstant.KEY_EMPLOYEE.equals(keyType)) {
            return PREFIX_KEY_EMPLOYEE + tenantName + ":" + posId;
        } else if (RedisConstant.KEY_POS.equals(keyType)) {
            return PREFIX_KEY_POS + tenantName + ":" + posId;
        } else {
            return PREFIX_KEY_VIEW + tenantName + ":" + posId;
        }
    }

    public <T> void put(String key, T value, String... ignoreFields) {
        Map valueMap = objectMapper.convertValue(value, Map.class);

        for (String field : ignoreFields) {
            valueMap.remove(field);
        }

        HashMap map = new HashMap<>(valueMap);
        redisTemplate.opsForHash().putAll(key, map);
    }

    public <T> T get(String key, Class<T> clazz) {
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        return objectMapper.convertValue(map, clazz);
    }

    public List<String> getKeys(String pattern) {
        return new ArrayList<>(Objects.requireNonNull(redisTemplate.keys(pattern)));
    }

    public void remove(String key) {
        redisTemplate.delete(key);
    }

    public void remove(String key, String... fields) {
        redisTemplate.opsForHash().delete(key, (Object[]) fields);
    }
}
