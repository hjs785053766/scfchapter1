package com.forezp.api.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface HiServic {
    @GetMapping(value = "/hi")
    String home(@RequestParam(value = "name") String name);
}
