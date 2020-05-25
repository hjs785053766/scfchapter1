package com.forezp.serviceauth.service;

import com.forezp.serviceauth.entity.UserInfo;

public interface UserInfoService {
    /** 通过username查找用户信息；*/
    public UserInfo findByUsername(String username);
}
