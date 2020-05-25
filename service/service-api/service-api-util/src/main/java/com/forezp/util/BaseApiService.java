package com.forezp.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.Objects;

@Slf4j
public class BaseApiService {


    /**
     * 获取上下文请求
     *
     * @return 请求对象
     */
    protected HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }

    protected HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
    }

    /**
     * @description 获取请求的Cliect的IP地址
     * @Return IP地址
     */
    protected String getCliectIp() {
        try {
            HttpServletRequest request = getRequest();
            String ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.trim() == "" || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.trim() == "" || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.trim() == "" || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }

            // 多个路由时，取第一个非unknown的ip
            final String[] arr = ip.split(",");
            for (final String str : arr) {
                if (!"unknown".equalsIgnoreCase(str)) {
                    ip = str;
                    break;
                }
            }
            return ip;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 从请求的header中拿到用户名
     *
     * @return 用户名
     */
    protected String getUserName() {
        try {
            String userName = getRequest().getHeader("username");
            return userName == null ? "" : URLDecoder.decode(userName, "utf8");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
