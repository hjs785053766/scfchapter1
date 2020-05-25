package com.forezp.serviceauth.controller;

import com.forezp.serviceauth.entity.UserInfo;
import com.forezp.serviceauth.service.UserInfoService;
import com.forezp.serviceauth.utils.JWTUtils;
import com.forezp.util.Notice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Enumeration;

@Api(tags = "登录服务")
@RestController
public class UserInfoController {

    @Resource
    UserInfoService userInfoService;

    @Resource
    private JWTUtils jwtUtils;

    /**
     * 获取token
     *
     * @param username 账户
     * @return
     */
    @GetMapping("/getToken")
    @ApiOperation("获取token")
    public Notice getToken(@RequestParam String username) throws Exception {
        UserInfo userInfo = userInfoService.findByUsername(username);
        return new Notice(HttpStatus.OK, jwtUtils.generateToken(userInfo), "成功");
    }

    /**
     * 验证token
     *
     * @return
     */
    @GetMapping("/verifyToken")
    @ApiOperation("验证token")
    public Notice verifyToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 获取指定的请求数据
        response.setContentType("text/html;charset=utf-8");
        System.out.println(request.getHeader("token"));
        return jwtUtils.verification(request.getHeader("token"));
    }
}