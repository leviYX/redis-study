package com.levi.utils;

public class RedisKeysUtil {

    private static final String USER_PREFIX = "users#";
    private static final String SESSIONS_PREFIX = "sessions#";

    public static String buildUsersKey(String userId){
        return USER_PREFIX + userId;
    }

    public static String buildSessionsKey(String sessionId){
        return SESSIONS_PREFIX + sessionId;
    }
}
