package org.example;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
//import com.auth0.jwt.interfaces.Clock;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.io.UnsupportedEncodingException;
//import java.util.Date;

public class JsonWebToken {
    public static void parseJWT(String token) throws UnsupportedEncodingException {
        Algorithm algorithm = Algorithm.HMAC256("secret");
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("auth0")
                .build(); //Reusable verifier instance
        DecodedJWT jwt = verifier.verify(token);
    }
/*    public static void parseJWT2(String token) throws UnsupportedEncodingException {
//        Clock clock = new Clock() {
//            @Override
//            public Date getToday() {
//                return new Date();
//            }
//        };
//        Date date = new Date();
        Algorithm algorithm = Algorithm.HMAC256("secret2");
        JWTVerifier verifier = JWT.require(algorithm)
                .
*//*        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWT.require(algorithm);
//        JWTVerifier verifier = verification.build(clock);
        JWTVerifier verifier = verification.build();*//*
        DecodedJWT jwt = verifier.verify(token);
    }*/
}
