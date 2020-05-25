package com.forezp.api.controller;

import com.forezp.api.service.HiServic;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "hi服务接口")
@RestController
public class HiController implements HiServic {
    @Value("${server.port}")
    String port;


    @Override
    @ApiOperation("查询当前请求端口")
    public String home(String name) {
        return "hi " + name + " ,i am from port:" + port;
    }
}
