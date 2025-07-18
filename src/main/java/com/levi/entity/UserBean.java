package com.levi.entity;

import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserBean {
    private String id;
    private String username;
    private String password;
    private Integer age;
    private String phone;
    private String webSite;
}
