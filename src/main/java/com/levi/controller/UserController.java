package com.levi.controller;

import com.levi.entity.UserBean;
import com.levi.service.UserService;

public class UserController {

    public static void main(String[] args) {
//        var uid = createUser(new UserBean("1", "levi", "234567",25, "13888888888", "https://www.juejin.cn/user/1187543767143183"));
//        System.out.printf("用户%s注册成功",uid);
        var user = getUserById("1");
        System.out.println(user);
    }

    /**
     * redis中存储创建的用户为hash结构
     *
     * @param user
     * @return
     */
    public static String  createUser(UserBean user) {
        UserService userService = new UserService();
        return userService.createUser(user);
    }

    /**
     * 根据用户id获取用户信息
     *
     * @param userId
     * @return
     */
    public static UserBean getUserById(String userId){
        UserService userService = new UserService();
        return userService.getUserById(userId);
    }
}
