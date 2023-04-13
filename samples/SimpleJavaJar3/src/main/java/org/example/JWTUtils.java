package org.example;

import com.auth0.jwt.JWT;
//import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
//import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
//import java.util.Map;

/**
 * @author syb date 2020/10/23
 */
public class JWTUtils {
    private static final String KEY = "secret";

  /*  public static String getJWT(String id,String name,Map<String,String> map2){
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND,60);


        //定义加密的算法，添加一个秘钥
        Algorithm algorithmHS = Algorithm.HMAC256(KEY);
        Map map = new HashMap<>();
        map.put("alg","HS256");//加密的算法
        map.put("typ","jwt");//令牌类型 通常为jwt
        //头部部分
        JWTCreator.Builder builder = JWT.create();
        map2.forEach((k,v)->{
            builder.withClaim(k,v);
        });

        String token = builder.withHeader(map)
                //创建的主体
                .withSubject(name)
                .withJWTId(id)
                //令牌签发时间
                .withIssuedAt(new Date())
                //令牌过期时间
                .withExpiresAt(instance.getTime())
                .sign(algorithmHS);
        return token;
    }*/

    public static DecodedJWT getTokenInfo(String token) throws UnsupportedEncodingException {
        List list = new ArrayList();
//        Map map = new HashMap<>();
        Algorithm algorithm = Algorithm.HMAC256(KEY);
        //根据创建者获得一个解析器
        JWTVerifier bao = JWT.require(algorithm).build();
        //解析token
        DecodedJWT verify = bao.verify(token);
        return verify;
    }
}