package com.forezp.servicegateway.controller;

import com.forezp.servicegateway.utils.Notice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 熔断成功跳转接口
 */
@RestController
public class FallBackController {

    @GetMapping("/fallback")
    public Notice fallback() {
        return new Notice(HttpStatus.INTERNAL_SERVER_ERROR, "失败，服务被关闭");
    }

    @GetMapping("/error")
    public Notice error() {
        return new Notice(HttpStatus.UNAUTHORIZED, "失败，请登录");
    }

    @GetMapping("/jurisdiction")
    public Notice jurisdiction() {
        return new Notice(HttpStatus.UNAUTHORIZED, "失败，无权限");
    }
}
