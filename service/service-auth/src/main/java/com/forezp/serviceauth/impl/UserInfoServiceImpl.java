package com.forezp.serviceauth.impl;

import com.forezp.serviceauth.dao.UserInfoDao;
import com.forezp.serviceauth.entity.UserInfo;
import com.forezp.serviceauth.service.UserInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    UserInfoDao userInfoDao;

    @Override
    public UserInfo findByUsername(String username) {
        return userInfoDao.findByUsername(username);
    }
}