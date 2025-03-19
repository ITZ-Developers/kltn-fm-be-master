package com.master.service;

import com.master.constant.MasterConstant;
import com.master.dto.account.AccountAdminDto;
import com.master.dto.customer.CustomerAdminDto;
import com.master.model.Account;
import com.master.model.Group;
import com.master.model.Location;
import com.master.rabbit.RabbitService;
import com.master.rabbit.form.LockAccountRequest;
import com.master.rabbit.form.ProcessTenantForm;
import com.master.redis.RedisConstant;
import com.master.redis.RedisService;
import com.master.redis.dto.SessionDto;
import com.master.repository.AccountRepository;
import com.master.repository.LocationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SessionService {
    @Autowired
    private RedisService redisService;
    @Autowired
    private RabbitService rabbitService;
    @Value("${rabbitmq.queue.notification}")
    private String notificationQueue;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private AccountRepository accountRepository;

    public void mappingLastLoginForAccount(AccountAdminDto account) {
        Integer keyType = MasterConstant.USER_KIND_ADMIN.equals(account.getKind()) ? RedisConstant.KEY_ADMIN : RedisConstant.KEY_CUSTOMER;
        String key = redisService.getKeyString(keyType, account.getUsername(), null);
        SessionDto dto = redisService.get(key, SessionDto.class);
        if (dto != null && dto.getTime() != null) {
            account.setLastLogin(dto.getTime());
        }
    }

    public void mappingLastLoginForListAccounts(List<AccountAdminDto> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return;
        }
        Map<String, AccountAdminDto> keyToAccountMap = new HashMap<>();
        for (AccountAdminDto account : accounts) {
            Integer keyType = MasterConstant.USER_KIND_ADMIN.equals(account.getKind())
                    ? RedisConstant.KEY_ADMIN
                    : RedisConstant.KEY_CUSTOMER;
            String key = redisService.getKeyString(keyType, account.getUsername(), null);
            keyToAccountMap.put(key, account);
        }
        Map<String, SessionDto> sessionData = redisService.multiGet(keyToAccountMap.keySet(), SessionDto.class);
        for (Map.Entry<String, AccountAdminDto> entry : keyToAccountMap.entrySet()) {
            SessionDto dto = sessionData.get(entry.getKey());
            if (dto != null && dto.getTime() != null) {
                entry.getValue().setLastLogin(dto.getTime());
            }
        }
    }

    public void mappingLastLoginForCustomer(CustomerAdminDto account) {
        String key = redisService.getKeyString(RedisConstant.KEY_CUSTOMER, account.getAccount().getUsername(), null);
        SessionDto dto = redisService.get(key, SessionDto.class);
        if (dto != null && dto.getTime() != null) {
            account.getAccount().setLastLogin(dto.getTime());
        }
    }

    public void mappingLastLoginForListCustomers(List<CustomerAdminDto> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return;
        }
        Map<String, CustomerAdminDto> keyToAccountMap = new HashMap<>();
        for (CustomerAdminDto account : accounts) {
            String key = redisService.getKeyString(RedisConstant.KEY_CUSTOMER, account.getAccount().getUsername(), null);
            keyToAccountMap.put(key, account);
        }
        Map<String, SessionDto> sessionData = redisService.multiGet(keyToAccountMap.keySet(), SessionDto.class);
        for (Map.Entry<String, CustomerAdminDto> entry : keyToAccountMap.entrySet()) {
            SessionDto dto = sessionData.get(entry.getKey());
            if (dto != null && dto.getTime() != null) {
                entry.getValue().getAccount().setLastLogin(dto.getTime());
            }
        }
    }

    public void sendMessageLockAccount(String username, Integer userKind, String tenantName) {
        LockAccountRequest request = new LockAccountRequest();
        request.setApp(MasterConstant.APP_MASTER);
        request.setUsername(username);
        request.setUserKind(userKind);
        request.setTenantName(tenantName);

        String key = redisService.getKeyString(userKind, username, tenantName);
        SessionDto dto = redisService.get(key, SessionDto.class);
        if (dto != null) {
            ProcessTenantForm<LockAccountRequest> form = new ProcessTenantForm<>();
            form.setAppName(MasterConstant.BACKEND_APP);
            form.setQueueName(notificationQueue);
            form.setCmd(MasterConstant.CMD_LOCK_DEVICE);
            form.setData(request);
            redisService.remove(key);
            rabbitService.handleSendMsg(form);
        }
    }

    public void sendMessageLockAccountsByTenantId(String tenantName) {
        String keyPattern = RedisService.PREFIX_KEY_EMPLOYEE + tenantName + ":*";
        redisService.getKeys(keyPattern).forEach(key -> {
            SessionDto dto = redisService.get(key, SessionDto.class);
            if (dto != null) {
                String[] keyParts = key.split(":");
                if (keyParts.length > 2 && StringUtils.isNotBlank(keyParts[2])) {
                    String username = keyParts[2];

                    LockAccountRequest request = new LockAccountRequest();
                    request.setApp(MasterConstant.APP_MASTER);
                    request.setUsername(username);
                    request.setUserKind(MasterConstant.USER_KIND_EMPLOYEE);
                    request.setTenantName(tenantName);

                    ProcessTenantForm<LockAccountRequest> form = new ProcessTenantForm<>();
                    form.setAppName(MasterConstant.BACKEND_APP);
                    form.setQueueName(notificationQueue);
                    form.setCmd(MasterConstant.CMD_LOCK_DEVICE);
                    form.setData(request);
                    redisService.remove(key);
                    rabbitService.handleSendMsg(form);
                }
            }
        });
    }

    public void sendMessageLockLocation(Location location) {
        Account account = location.getCustomer().getAccount();
        sendMessageLockAccount(account.getUsername(), account.getKind(), null);
        String tenantName = location.getTenantId();
        sendMessageLockAccountsByTenantId(tenantName);
    }

    public void sendMessageLockAccountByGroup(Group group) {
        if (MasterConstant.USER_KIND_EMPLOYEE.equals(group.getKind())) {
            List<String> tenantNames = locationRepository.findAllDistinctTenantId();
            tenantNames.forEach(this::sendMessageLockAccountsByTenantId);
        } else {
            List<Account> accounts = accountRepository.findAllByGroupId(group.getId());
            accounts.forEach(account -> sendMessageLockAccount(account.getUsername(), account.getKind(), null));
        }
    }
}
