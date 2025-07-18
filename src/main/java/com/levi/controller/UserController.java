package com.levi.controller;

import com.levi.entity.UserBean;
import com.levi.service.UserService;

public class UserController {

    public static void main(String[] args) {
        var uid = createUser(new UserBean("1", "levi", "234567",25, "13888888888", "https://www.juejin.cn/user/1187543767143183"));
        System.out.printf("用户%s注册成功",uid);
    }

    public static String  createUser(UserBean user) {
        UserService userService = new UserService();
        return userService.createUser(user);
    }
}
