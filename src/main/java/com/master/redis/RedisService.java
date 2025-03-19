package com.master.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.master.model.Account;
import com.master.repository.AccountRepository;
import com.master.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private static final String PREFIX_KEY_ADMIN = "adm:";
    private static final String PREFIX_KEY_CUSTOMER = "cus:";
    public static final String PREFIX_KEY_EMPLOYEE = "emp:";
    public static final int TTL = 2592000; // 30 days
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private AccountRepository accountRepository;

    public String getKeyString(Integer keyType, String username, String tenantName) {
        if (RedisConstant.KEY_ADMIN.equals(keyType)) {
            return PREFIX_KEY_ADMIN + username;
        } else if (RedisConstant.KEY_CUSTOMER.equals(keyType)) {
            return PREFIX_KEY_CUSTOMER + username;
        } else {
            return PREFIX_KEY_EMPLOYEE + tenantName + ":" + username;
        }
    }

    public <T> void put(String key, T value, String... ignoreFields) {
        Map valueMap = objectMapper.convertValue(value, Map.class);

        for (String field : ignoreFields) {
            valueMap.remove(field);
        }

        HashMap map = new HashMap<>(valueMap);
        redisTemplate.opsForHash().putAll(key, map);
        // Set TTL for the Redis key
        redisTemplate.expire(key, TTL, TimeUnit.SECONDS);
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

    public <T> Map<String, T> multiGet(Collection<String> keys, Class<T> clazz) {
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        Map<String, T> resultMap = new HashMap<>();
        int index = 0;
        for (String key : keys) {
            if (values.get(index) != null) {
                resultMap.put(key, (T) values.get(index));
            }
            index++;
        }
        return resultMap;
    }

    public void sendMessageLockAccount(String username, Integer userKind, String tenantName) {
        Account account = accountRepository.findFirstByUsernameAndKind(username, userKind).orElse(null);
        if (account != null) {
            account.setLastLogin(new Date());
            accountRepository.save(account);
        }
        sessionService.sendMessageLockAccount(username, userKind, tenantName);
    }
}
