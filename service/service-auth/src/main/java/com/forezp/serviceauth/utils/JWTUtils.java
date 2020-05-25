package com.forezp.serviceauth.utils;

import com.alibaba.fastjson.JSON;
import com.forezp.serviceauth.entity.SysPermission;
import com.forezp.serviceauth.entity.SysRole;
import com.forezp.serviceauth.entity.UserInfo;
import com.forezp.util.Notice;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JWTUtils {
    /**
     * 秘钥
     */
    @Value("${jwt.secret}")
    String secret;

    /**
     * 有效期，单位秒
     */
    @Value("${jwt.expirationTimeInSecond}")
    Long expirationTimeInSecond;

    /**
     * 自定义密钥
     * Base64加密key
     */
    @Value("${Base64.key}")
    String EncryptKey;

    @Resource
    private RedisUtil redisUtil;

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
            log.error("token解析错误", e);
            throw new IllegalArgumentException("Token invalided.");
        }
    }

    /**
     * 获取token的过期时间
     *
     * @param token token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token)
                .getExpiration();
    }

    /**
     * 判断token是否过期
     *
     * @param token token
     * @return 已过期返回true，未过期返回false
     */
    private Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 计算token的过期时间
     *
     * @return 过期时间
     */
    private Date getExpirationTime() {
        return new Date(System.currentTimeMillis() + this.expirationTimeInSecond * 1000);
    }

    /**
     * 判断token是否非法
     *
     * @param token token
     * @return 未过期返回true，否则返回false
     */
    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    /**
     * 为指定用户生成token
     *
     * @param user 用户信息
     * @return token
     */
    public String generateToken(UserInfo user) throws Exception {
        EncryptUtil des = EncryptUtil.getEncryptUtil(EncryptKey, "utf-8");
        String AfterEncryption = des.encode(user.getId().toString());
        Map<String, Object> claims = new HashMap<String, Object>();
        Map<String, String> url = new HashMap<String, String>();
        for (SysRole sysRole : user.getRoles()) {
            for (SysPermission sysPermission : sysRole.getPermissions()) {
                url.put(sysPermission.getUrl(), sysPermission.getUrl());
            }
            sysRole.setPermissions(null);
        }
        claims.put("user", user);
        claims.put("GenerationTime", new Date().getTime() / 1000);
        claims.put("url", url);
        String claimsjson = JSON.toJSONString(claims);
        Date createdTime = new Date();
        Date expirationTime = this.getExpirationTime();
        byte[] keyBytes = secret.getBytes();
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        JwtBuilder jwtBuilder = Jwts.builder();
        Map map = new HashMap();
        map.put("key", AfterEncryption);
        jwtBuilder.setClaims(map);
        jwtBuilder.setIssuedAt(createdTime);
        jwtBuilder.setExpiration(expirationTime);
        // 你也可以改用你喜欢的算法
        // 支持的算法详见：https://github.com/jwtk/jjwt#features
        jwtBuilder.signWith(key, SignatureAlgorithm.HS256);
        // 设置头部信息
        Map<String, Object> header = new HashMap<>(2);
        header.put("Type", "Jwt");
        header.put("GenerationTime", new Date().getTime() / 1000 + "");
        jwtBuilder.setHeaderParams(header);
        String token = jwtBuilder.compact();
        if (redisUtil.hasKey(AfterEncryption)) {
            redisUtil.del(AfterEncryption);
        }
        redisUtil.set(AfterEncryption, claimsjson, expirationTimeInSecond);
        token = token.replaceAll("\r|\n", "");
        return token;
    }

    /**
     * 验证token是否合法以及过期，未过期返回用户json
     *
     * @param token
     * @return
     * @throws Exception
     */
    public Notice verification(String token) throws Exception {
        // 验证token是否被修改
        try {
            validateToken(token);
        } catch (Exception e) {
            // TODO: handle exception
            return new Notice(HttpStatus.INTERNAL_SERVER_ERROR, "token错误");
        }

        String someToken = token;
        // 测试2: 如果能token合法且未过期，返回true
        Boolean validateToken = validateToken(someToken);
        if (!validateToken) {
            return new Notice(HttpStatus.INTERNAL_SERVER_ERROR, "token过期");
        }
        return new Notice(HttpStatus.OK, "成功");
    }
}