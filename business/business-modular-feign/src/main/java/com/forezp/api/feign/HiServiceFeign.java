package com.forezp.api.feign;

import com.forezp.api.service.HiServic;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "business-modular-hi")
public interface HiServiceFeign extends HiServic {
}
