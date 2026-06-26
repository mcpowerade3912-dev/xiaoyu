package com.xiaoyu.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    //加密密钥
    private final SecretKey secret = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    //有效期两小时
    private final long expiration = 2 * 60 * 60 * 1000;

    //生成token:存入id,和角色
    public String generateToken(Long userid,Integer role) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + expiration);
        String token = Jwts.builder()
                //设置头信息
                .setHeaderParam("typ", "JWT")
                //设置主体
                .setSubject("xiaoyu")
                //存放用户id和角色
                .claim("userId", userid)
                .claim("role", role)
                //设置签发时间
                .setIssuedAt(now)
                //设置过期时间
                .setExpiration(expireTime)
                //设置加密方式
                .signWith(SignatureAlgorithm.HS512, secret)
                //创建token
                .compact();
      return token;
    }
    public Claims parseToken(String token) {
        Claims claims = null;
        try {
            //.parser()解析器与builder对应
            claims = Jwts.parser()
                    //设置解密密钥
                    .setSigningKey(secret)
                    //解析 token，包括主体、头信息、签名信息
                    .parseClaimsJws(token)
                    // 取出数据
                    .getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //返回数据
        return claims;
    }

}
