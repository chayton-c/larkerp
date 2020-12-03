package com.yingda.lkj.utils.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.yingda.lkj.beans.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author hood  2019/12/16
 */
public class JWTUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JWTUtil.class);

    // 过期时间 18h
    private static final long EXPIRE_TIME = 18 * 3600L * 1000;
//    private static final long EXPIRE_TIME = 100L;
    // 密钥
    private static final String SECRET = "gongshenhuoerdi"; // 公审霍尔蒂，建设匈牙利

    /**
     * 生成 token, EXPIRE_TIME 后过期
     *
     * @param username 用户名
     * @return 加密的token
     */
    public static String createToken(String username) {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        Algorithm algorithm = Algorithm.HMAC256(SECRET);
        // 附带username信息
        return JWT.create()
                .withClaim("username", username)
                .withClaim("projectName", Constant.projectName)
                //到期时间
                .withExpiresAt(date)
                //创建一个新的JWT，并使用给定的算法进行标记
                .sign(algorithm);
    }

    /**
     * 校验 token 是否正确
     */
    public static boolean verify(String token, String username) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            // 在token中附带了username信息
            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaim("username", username)
                    .withClaim("projectName", Constant.projectName)
                    .build();
            verifier.verify(token); // 验证 token
            return true;
        } catch (Exception e) {
            LOGGER.error("error in JWTUtil.verify", e);
            return false;
        }
    }

    /**
     * 获得token中的信息，无需secret解密也能获得
     *
     * @return token中包含的用户名
     */
    public static String getUsernameFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("username").asString();
        } catch (Exception e) {
            LOGGER.error("error in JWTUtil.getUsername", e);
            return null;
        }
    }

    public static void main(String[] args) {
        String token = createToken("akagi");
        verify(token, "taiho");

        long current = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++)
            getUsernameFromToken(token);
        System.out.println(System.currentTimeMillis() - current);
    }
}
