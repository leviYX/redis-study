package com.levi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levi.RedisClientFactory;
import com.levi.entity.LoginUser;
import com.levi.utils.RedisKeysUtil;
import io.lettuce.core.RedisFuture;
import org.apache.commons.beanutils.BeanUtils;

import java.util.Map;
import java.util.UUID;

public class SessionService {

    public String saveSession(LoginUser user){
        try {
            // check user exists todo
            String sessionId = UUID.randomUUID().toString().replaceAll("-","");
            String sessionsKey = RedisKeysUtil.buildSessionsKey(sessionId);
            var syncCommands = RedisClientFactory.getSyncCommands();
            Map<String, String> userMap = BeanUtils.describe(user);
            syncCommands.hmset(sessionsKey, userMap);
            return sessionId;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            RedisClientFactory.close();
        }
        return null;
    }

    public LoginUser getSessionById(String sessionId){
        validateParam(sessionId);
        try {
            String sessionsKey = RedisKeysUtil.buildSessionsKey(sessionId);
            var syncCommands = RedisClientFactory.getSyncCommands();
            RedisFuture<Map<String, String>> userMapFuture = syncCommands.hgetall(sessionsKey);
            Map<String, String> userMap = userMapFuture.get();
            if(userMap == null) return null;
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.convertValue(userMap, LoginUser.class);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            RedisClientFactory.close();
        }
        return null;
    }

    private void validateParam(String id){
        if(id == null) throw new IllegalArgumentException("id is null");
    }
}
