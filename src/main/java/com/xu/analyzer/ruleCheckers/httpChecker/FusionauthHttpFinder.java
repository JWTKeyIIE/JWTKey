package com.xu.analyzer.ruleCheckers.httpChecker;

import com.xu.analyzer.ruleCheckers.Criteria;

import java.util.ArrayList;
import java.util.List;

public class FusionauthHttpFinder extends UnsecureHttpFinder{
    private static final List<Criteria> CRITERIA_LIST = new ArrayList<>();
    static {
        Criteria criteria0 = new Criteria();
        criteria0.setClassName("io.fusionauth.jwks.JSONWebKeySetHelper");
        criteria0.setMethodName("java.util.List retrieveKeysFromIssuer(java.lang.String)");
        criteria0.setParam(0);
        CRITERIA_LIST.add(criteria0);
        Criteria criteria1 = new Criteria();
        criteria1.setClassName("io.fusionauth.jwks.JSONWebKeySetHelper");
        criteria1.setMethodName("java.util.List retrieveKeysFromIssuer(java.lang.String,java.util.function.Consumer)");
        criteria1.setParam(0);
        CRITERIA_LIST.add(criteria1);
        Criteria criteria2 = new Criteria();
        criteria2.setClassName("io.fusionauth.jwks.JSONWebKeySetHelper");
        criteria2.setMethodName("java.util.List retrieveKeysFromWellKnownConfiguration(java.net.HttpURLConnection)");
        criteria2.setParam(0);
        CRITERIA_LIST.add(criteria2);
        Criteria criteria3 = new Criteria();
        criteria3.setClassName("io.fusionauth.jwks.JSONWebKeySetHelper");
        criteria3.setMethodName("java.util.List retrieveKeysFromWellKnownConfiguration(java.lang.String)");
        criteria3.setParam(0);
        CRITERIA_LIST.add(criteria3);
        Criteria criteria4 = new Criteria();
        criteria4.setClassName("io.fusionauth.jwks.JSONWebKeySetHelper");
        criteria4.setMethodName("java.util.List retrieveKeysFromWellKnownConfiguration(java.lang.String,java.util.function.Consumer)");
        criteria4.setParam(0);
        CRITERIA_LIST.add(criteria4);
        Criteria criteria5 = new Criteria();
        criteria5.setClassName("io.fusionauth.jwks.JSONWebKeySetHelper");
        criteria5.setMethodName("java.util.List retrieveKeysFromJWKS(java.lang.String)>");
        criteria5.setParam(0);
        CRITERIA_LIST.add(criteria5);
        Criteria criteria6 = new Criteria();
        criteria6.setClassName("io.fusionauth.jwks.JSONWebKeySetHelper");
        criteria6.setMethodName("java.util.List retrieveKeysFromJWKS(java.lang.String)>");
        criteria6.setParam(0);
        CRITERIA_LIST.add(criteria6);
        Criteria criteria7 = new Criteria();
        criteria7.setClassName("io.fusionauth.jwks.JSONWebKeySetHelper");
        criteria7.setMethodName("java.util.List retrieveKeysFromJWKS(java.lang.String,java.util.function.Consumer)");
        criteria7.setParam(0);
        CRITERIA_LIST.add(criteria7);
        Criteria criteria8 = new Criteria();
        criteria8.setClassName("io.fusionauth.jwks.JSONWebKeySetHelper");
        criteria8.setMethodName("java.util.List retrieveKeysFromJWKS(java.net.HttpURLConnection)");
        criteria8.setParam(0);
        CRITERIA_LIST.add(criteria8);

//        CRITERIA_CLASSES.add("io.fusionauth.jwks.JSONWebKeySetHelper");
//        CRITERIA_CLASSES.add("io.fusionauth.jwks.domain.JSONWebKey");
    }

    @Override
    public List<Criteria> getCriteriaList() {
        return CRITERIA_LIST;
    }

    @Override
    public String getJwtLibName() {
        return "FusionAuth";
    }
}
