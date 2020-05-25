package com.forezp.api.service;

import com.forezp.util.Notice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface FeignServic {
    @GetMapping(value = "/home")
    Notice home(@RequestParam(value = "name") String name);
}
