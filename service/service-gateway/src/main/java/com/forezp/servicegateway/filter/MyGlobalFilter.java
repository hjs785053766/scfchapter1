package com.forezp.servicegateway.filter;

import com.alibaba.fastjson.JSON;
import com.forezp.servicegateway.utils.RedisUtil;
import io.jsonwebtoken.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 全局过滤器
 * 校验token
 */
public class MyGlobalFilter implements GlobalFilter, Ordered {

    private static final Log log = LogFactory.getLog(MyGlobalFilter.class);

    @Resource
    private RedisUtil redisUtil;

    /**
     * 秘钥
     */
    @Value("${jwt.secret}")
    String secret;

    private static final String TOKEN = "token";

    /**
     * 请求白名单
     */
    private String[] skipAuthUrls = {"/auth/getToken", "/auth/verifyToken"};

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String url = exchange.getRequest().getURI().getPath();

        //判断是否是Knife4j请求
        boolean urlJudge = url.contains("/v2/api-docs");
        if (urlJudge) {
            return chain.filter(exchange);
        }
        //查询白名单
        if (null != skipAuthUrls && Arrays.asList(skipAuthUrls).contains(url)) {
            return chain.filter(exchange);
        }
        //获取token
        List<String> tokenList = exchange.getRequest().getHeaders().get(TOKEN);
        String token = null;
        if (tokenList != null && tokenList.size() > 0) {
            token = tokenList.get(0);
        }
        if (StringUtils.isBlank(token)) {
            //没有token
            return returnAuthFail(exchange, 0);
        } else {
            Claims claims = getClaimsFromToken(token);
            String redisKey = "";
            if (claims == null) {
                return returnAuthFail(exchange, 0);
            }
            redisKey = claims.get("key").toString();
            String userJson = redisUtil.get(redisKey).toString();
            try {
                Map mapTypes = JSON.parseObject(userJson);
                Map urlMap = JSON.parseObject(mapTypes.get("url").toString());
                if (urlMap.get(url) != null) {
                    Map userMap = JSON.parseObject(mapTypes.get("user").toString());
                    ServerHttpRequest oldRequest = exchange.getRequest();
                    URI uri = oldRequest.getURI();
                    ServerHttpRequest.Builder builder = oldRequest.mutate().uri(uri);
                    //重写请求头部
                    builder.header("username", userMap.get("username").toString());
                    //重写请求头部
                    ServerHttpRequest newRequest = builder.build();
                    return chain.filter(exchange.mutate().request(newRequest).build());
                } else {
                    return returnAuthFail(exchange, 1);
                }
            } catch (Exception e) {
                return returnAuthFail(exchange, 0);
            }
        }
    }

    /**
     * 返回校验失败
     *
     * @param exchange
     * @return
     */
    private Mono<Void> returnAuthFail(ServerWebExchange exchange, int type) {
        String url = "http://127.0.0.1:8100/error";
        switch (type) {
            case 0:
                url = "http://127.0.0.1:8100/error";
                break;
            case 1:
                url = "http://127.0.0.1:8100/jurisdiction";
                break;
        }
        ServerHttpResponse response = exchange.getResponse();
        //303状态码表示由于请求对应的资源存在着另一个URI，应使用GET方法定向获取请求的资源
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().set(HttpHeaders.LOCATION, url);
        return response.setComplete();
    }

    /**
     * 从token中获取claim
     *
     * @param token token
     * @return claim
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(this.secret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}