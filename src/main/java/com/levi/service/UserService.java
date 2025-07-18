package com.levi.service;

import com.levi.RedisClientFactory;
import com.levi.entity.UserBean;
import org.apache.commons.beanutils.BeanUtils;

import java.util.Map;

public class UserService {

    private static final String USER_PREFIX = "users#";

    public String createUser(UserBean user){
        try {
            var syncCommands = RedisClientFactory.getSyncCommands();
            Map<String, String> userMap = BeanUtils.describe(user);
            syncCommands.hmset(USER_PREFIX + user.getId(), userMap);
            return user.getId();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            RedisClientFactory.close();
        }
        return null;
    }
}
