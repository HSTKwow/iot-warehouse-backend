package com.hstk.iot_warehouse.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {
    // 秘钥与过期时间改为环境变量/系统属性优先，避免硬编码敏感信息
    private static final String SECRET_KEY = getString("JWT_SECRET", "HSTK_WAREHOUSE_SECRET_KEY");
    // 默认为 12 小时（毫秒）
    private static final long EXPIRATION_TIME = getLong("JWT_EXPIRATION_MS", 12 * 60 * 60 * 1000L);

    /**
     * 生成 JWT 令牌
     * @param claims 自定义载荷数据
     * @return 令牌字符串
     */
    public static String generateJwt(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /**
     * 解析 JWT 令牌
     * @param jwt 令牌字符串
     * @return 载荷数据
     */
    public static Claims parseJwt(String jwt) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(jwt)
                .getBody();
    }

    private static String getString(String key, String defaultValue) {
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) {
            return env;
        }
        String prop = System.getProperty(key);
        if (prop != null && !prop.isBlank()) {
            return prop;
        }
        return defaultValue;
    }

    private static long getLong(String key, long defaultValue) {
        String text = getString(key, null);
        if (text == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
