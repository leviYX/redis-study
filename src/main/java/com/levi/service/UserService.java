package com.levi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levi.RedisClientFactory;
import com.levi.entity.UserBean;
import com.levi.utils.RedisKeysUtil;
import io.lettuce.core.RedisFuture;
import org.apache.commons.beanutils.BeanUtils;

import java.util.Map;

public class UserService {


    public String createUser(UserBean user){
        String userId = user.getId();
        validateUser(userId);
        try {
            String usersKey = RedisKeysUtil.buildUsersKey(userId);
            var syncCommands = RedisClientFactory.getSyncCommands();
            Map<String, String> userMap = BeanUtils.describe(user);
            syncCommands.hmset(usersKey, userMap);
            return userId;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            RedisClientFactory.close();
        }
        return null;
    }

    public UserBean getUserById(String userId){
        validateUser(userId);
        try {
            String usersKey = RedisKeysUtil.buildUsersKey(userId);
            var syncCommands = RedisClientFactory.getSyncCommands();
            RedisFuture<Map<String, String>> userMapFuture = syncCommands.hgetall(usersKey);
            Map<String, String> userMap = userMapFuture.get();
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.convertValue(userMap, UserBean.class);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            RedisClientFactory.close();
        }
        return null;
    }

    private void validateUser(String userId){
        if(userId == null) throw new IllegalArgumentException("user id is null");
    }


}
